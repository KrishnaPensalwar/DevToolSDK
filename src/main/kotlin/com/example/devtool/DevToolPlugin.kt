package com.example.devtool

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
import kotlin.coroutines.coroutineContext

/**
 * Mutable configuration for [DevToolPlugin].
 * Keep mutable fields and read them on each request so [DevToolSdk] can toggle mocking at runtime.
 */
class DevToolConfig {
    /** When true, matched (or default) mock responses are returned and the request never hits the network. */
    var mockingEnabled: Boolean = true

    /** Return a [MockResponse] to short-circuit, or null to use the built-in default mock body. */
    var mockResolver: (HttpRequestBuilder) -> MockResponse? = { null }

    var requestModifier: (HttpRequestBuilder) -> Unit = {}

    var responseObserver: (HttpResponse) -> Unit = {}

    var recorder: ((HttpRequest, HttpResponse) -> Unit)? = null
}

/**
 * Mock HTTP response returned by the DevTool plugin when mocking is enabled.
 */
data class MockResponse(
    val status: HttpStatusCode = HttpStatusCode.OK,
    val headers: Headers = headersOf(
        HttpHeaders.ContentType,
        ContentType.Application.Json.toString()
    ),
    val body: ByteReadChannel = ByteReadChannel("{}")
) {
    constructor(
        status: HttpStatusCode = HttpStatusCode.OK,
        headers: Headers = headersOf(
            HttpHeaders.ContentType,
            ContentType.Application.Json.toString()
        ),
        body: String
    ) : this(status, headers, ByteReadChannel(body))
}

val DevToolPlugin = createClientPlugin(
    name = "DevToolPlugin",
    createConfiguration = ::DevToolConfig
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
    }

    on(Send) { request ->
        if (!config.mockingEnabled) {
            return@on proceed(request)
        }

        val mock = config.mockResolver(request) ?: defaultMockResponse(request)
        buildMockCall(
            client = ktorClient,
            requestData = request.build(),
            mock = mock,
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
