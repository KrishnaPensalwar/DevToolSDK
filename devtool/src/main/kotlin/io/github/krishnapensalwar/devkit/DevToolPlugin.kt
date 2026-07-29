package io.github.krishnapensalwar.devkit

import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodedPath
import io.ktor.http.headersOf
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.ktor.client.statement.bodyAsText

/**
 * Configuration options for [DevToolPlugin].
 */
class KtorDevToolConfig {
    /** When true, matched or cached mock responses are returned without making a live network call. */
    var mockingEnabled: Boolean = true

    /** Lambda function to resolve a custom [MockResponse] for a given [HttpRequestBuilder], or `null` to use cached/default mocks. */
    var mockResolver: (HttpRequestBuilder) -> MockResponse? = { null }

    /** Lambda block to modify outgoing requests. */
    var requestModifier: (HttpRequestBuilder) -> Unit = {}

    /** Lambda block to observe incoming responses. */
    var responseObserver: (HttpResponse) -> Unit = {}

    /** Optional recorder callback for logging request and response pairs. */
    var recorder: ((HttpRequest, HttpResponse) -> Unit)? = null
}

/**
 * Represents a mock HTTP response returned by [DevToolPlugin] when network mocking is enabled.
 *
 * @property status HTTP status code of the mock response. Defaults to [HttpStatusCode.OK].
 * @property headers HTTP headers of the mock response. Defaults to `Content-Type: application/json`.
 * @property body Content stream of the mock response body.
 */
data class MockResponse(
    val status: HttpStatusCode = HttpStatusCode.OK,
    val headers: Headers = headersOf(
        HttpHeaders.ContentType,
        ContentType.Application.Json.toString()
    ),
    val body: ByteReadChannel = ByteReadChannel("{}")
) {
    /**
     * Constructs a [MockResponse] with a String body.
     *
     * @param status HTTP status code. Defaults to [HttpStatusCode.OK].
     * @param headers HTTP headers. Defaults to `Content-Type: application/json`.
     * @param body String content for the response body.
     */
    constructor(
        status: HttpStatusCode = HttpStatusCode.OK,
        headers: Headers = headersOf(
            HttpHeaders.ContentType,
            ContentType.Application.Json.toString()
        ),
        body: String
    ) : this(status, headers, ByteReadChannel(body))
}

/**
 * Ktor [io.ktor.client.plugins.api.ClientPlugin] for inspecting, logging, caching, and mocking network traffic.
 */
val DevToolPlugin = createClientPlugin(
    name = "DevToolPlugin",
    createConfiguration = ::KtorDevToolConfig
) {
    // Cache config locally as recommended by Ktor; also avoids implicit-receiver
    // resolution issues inside the `on(Send)` hook below.
    val config = pluginConfig
    val ktorClient = client

    DevToolSdk.bind(config, ktorClient)

    onRequest { request, _ ->
        config.requestModifier(request)
    }

    onResponse { response ->
        config.responseObserver(response)
        config.recorder?.invoke(response.call.request, response)
        // Save to cache when mocking enabled
        if (config.mockingEnabled) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val bodyText = response.bodyAsText()
                    io.github.krishnapensalwar.devkit.cache.CacheManager.save(
                        url = response.call.request.url.toString(),
                        method = response.call.request.method.value,
                        status = response.status.value,
                        headers = response.headers,
                        body = bodyText
                    )
                } catch (e: Exception) {
                    // ignore cache errors
                }
            }
        }
    }

    on(Send) { request ->
        if (!config.mockingEnabled) {
            return@on proceed(request)
        }

        // Try custom mock resolver first
        val mock = config.mockResolver(request)
        if (mock != null) {
            return@on buildMockCall(
                client = ktorClient,
                requestData = request.build(),
                mock = mock,
                callContext = coroutineContext
            )
        }

        // Attempt to serve from cache
        val cached = io.github.krishnapensalwar.devkit.cache.CacheManager.get(request.url.toString(), request.method.value)
        if (cached != null) {
            val mockFromCache = io.github.krishnapensalwar.devkit.cache.CacheManager.toMockResponse(cached)
            return@on buildMockCall(
                client = ktorClient,
                requestData = request.build(),
                mock = mockFromCache,
                callContext = coroutineContext
            )
        }

        // No mock or cache, proceed to network
        // If mocking is enabled but no cache, respond with default mock
        val defaultMock = defaultMockResponse(request)
        return@on buildMockCall(
            client = ktorClient,
            requestData = request.build(),
            mock = defaultMock,
            callContext = coroutineContext
        )
    }
}

@OptIn(InternalAPI::class)
private fun buildMockCall(
    client: HttpClient,
    requestData: HttpRequestData,
    mock: MockResponse,
    callContext: CoroutineContext
): HttpClientCall {
    val responseData = HttpResponseData(
        statusCode = mock.status,
        requestTime = GMTDate(),
        headers = mock.headers,
        version = HttpProtocolVersion.HTTP_1_1,
        body = mock.body,
        callContext = callContext
    )
    return HttpClientCall(client, requestData, responseData)
}

private fun defaultMockResponse(request: HttpRequestBuilder): MockResponse {
    val path = request.url.encodedPath
    val body = when {
        path.contains("/driver/reports/assigned") -> "[]"
        path.contains("/devices/register") ->
            """{"message":"Device registered (mock)","deviceId":"mock-device","fcmToken":null,"deviceName":"mock"}"""
        path.contains("/complaints/") ->
            """
            {
              "id":"mock-complaint",
              "title":"Mock complaint",
              "description":"Served by DevTool mock",
              "status":"OPEN",
              "category":"OTHER",
              "imageUrls":[],
              "latitude":0.0,
              "longitude":0.0,
              "locationName":"Mock City",
              "createdAt":0,
              "updatedAt":0
            }
            """.trimIndent()
        else -> """{"mocked":true,"path":"$path"}"""
    }
    return MockResponse(body = body)
}