package io.github.krishnapensalwar.devkit

import io.github.krishnapensalwar.devkit.core.logging.LoggerManager
import io.github.krishnapensalwar.devkit.mock.MockDecision
import io.github.krishnapensalwar.devkit.mock.MockManager
import io.github.krishnapensalwar.devkit.mock.identity.RequestIdentity
import io.github.krishnapensalwar.devkit.mock.scenario.MockPlan
import io.github.krishnapensalwar.devkit.mock.toException
import io.github.krishnapensalwar.devkit.network.model.NetworkCall
import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.statement.HttpReceivePipeline
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
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

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
val StartTimeKey = io.ktor.util.AttributeKey<Long>("DevToolStartTime")

@OptIn(DelicateCoroutinesApi::class)
val DevToolPlugin = createClientPlugin(
    name = "DevToolPlugin",
    createConfiguration = ::KtorDevToolConfig
) {
    // Cache config locally as recommended by Ktor; also avoids implicit-receiver
    // resolution issues inside the `on(Send)` hook below.
    val config = pluginConfig
    val ktorClient = client

    DevToolSdk.bind(config, ktorClient)

    // Snapshot live body before observers / ContentNegotiation consume the channel.
    ktorClient.receivePipeline.intercept(HttpReceivePipeline.Before) { response ->
        val next = try {
            response.snapshotBodyForApp()
        } catch (_: Throwable) {
            response
        }
        proceedWith(next)
    }

    onRequest { request, _ ->
        request.attributes.put(StartTimeKey, System.nanoTime())
        config.requestModifier(request)
    }

    onResponse { response ->
        config.responseObserver(response)
        config.recorder?.invoke(response.call.request, response)

        persistNetworkCall(response)

        if (!MockManager.isMockingEnabled()) {
            val bodyText = response.call.attributes.getOrNull(CapturedResponseBodyKey) ?: return@onResponse
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val request = response.call.request
                    val requestBody = outgoingContentAsText(request.content)
                    val identity = RequestIdentity.parse(
                        url = request.url.toString(),
                        method = request.method.value,
                        requestBody = requestBody,
                        contentType = request.headers[HttpHeaders.ContentType]
                    )
                    if (identity.isGraphQl) {
                        io.github.krishnapensalwar.devkit.mock.scenario.MockScenarioRepository.saveSnapshotIfAbsent(
                            identity = identity,
                            status = response.status.value,
                            headersJson = "{}",
                            body = bodyText
                        )
                    }
                    io.github.krishnapensalwar.devkit.cache.CacheManager.save(
                        identity,
                        status = response.status.value,
                        headers = response.headers,
                        body = bodyText
                    )
                } catch (_: Exception) {
                }
            }
        }
    }

    on(Send) { request ->
        if (!MockManager.isMockingEnabled()) {
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

        val requestBodyText = when (val body = request.body) {
            is io.ktor.http.content.OutgoingContent -> outgoingContentAsText(body)
            is String -> body
            is ByteArray -> String(body, Charsets.UTF_8)
            else -> null
        }
        when (val decision = MockManager.decide(
            io.github.krishnapensalwar.devkit.mock.identity.RequestIdentity.parse(
                url = request.url.toString(),
                method = request.method.value,
                requestBody = requestBodyText,
                contentType = request.headers["Content-Type"]
            )
        )) {
            MockDecision.PassThrough -> return@on proceed(request)
            is MockDecision.Serve -> {
                if (decision.plan.delayMs > 0) {
                    kotlinx.coroutines.delay(decision.plan.delayMs.milliseconds)
                }
                if (decision.plan.failure != io.github.krishnapensalwar.devkit.mock.scenario.MockFailureKind.NONE) {
                    throw decision.plan.toException()
                }
                return@on buildMockCall(
                    client = ktorClient,
                    requestData = request.build(),
                    mock = decision.plan.toKtorMock(),
                    callContext = coroutineContext
                )
            }
            MockDecision.Missing -> {
                val path = request.url.encodedPath
                val errorBody = """
                    {
                      "error": "DevToolSDK Mocking Enabled",
                      "message": "Mocking is enabled in DevTool SDK, but no response has been cached or configured for this endpoint: $path. Please disable mocking or record/configure a mock response.",
                      "url": "${request.url}"
                    }
                """.trimIndent()
                val errorMock = MockResponse(
                    status = HttpStatusCode.NotFound,
                    headers = headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Application.Json.toString()
                    ),
                    body = errorBody
                )
                return@on buildMockCall(
                    client = ktorClient,
                    requestData = request.build(),
                    mock = errorMock,
                    callContext = coroutineContext
                )
            }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
private fun persistNetworkCall(response: HttpResponse) {
    val startTime = response.call.request.attributes.getOrNull(StartTimeKey)
    val duration = if (startTime != null) {
        TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
    } else {
        0L
    }

    val request = response.call.request
    val sensitive = DevTool.config.sensitiveHeaders
    val reqHeadersMap = mutableMapOf<String, String>()
    request.headers.forEach { key, values ->
        val isSensitive = sensitive.any { it.equals(key, ignoreCase = true) }
        reqHeadersMap[key] = if (isSensitive) "••••••••" else values.joinToString(",")
    }
    val resHeadersMap = mutableMapOf<String, String>()
    response.headers.forEach { key, values ->
        val isSensitive = sensitive.any { it.equals(key, ignoreCase = true) }
        resHeadersMap[key] = if (isSensitive) "••••••••" else values.joinToString(",")
    }

    val requestBodyText = outgoingContentAsText(request.content)
    val responseBodyText = response.call.attributes.getOrNull(CapturedResponseBodyKey)
        ?: "(body omitted)"

    val networkCall = NetworkCall(
        url = request.url.toString(),
        endpoint = request.url.encodedPath,
        host = request.url.host,
        method = request.method.value,
        requestHeaders = reqHeadersMap,
        requestBody = requestBodyText,
        requestSize = request.content.contentLength ?: 0L,
        responseHeaders = resHeadersMap,
        responseBody = responseBodyText,
        responseSize = responseBodyText.toByteArray(Charsets.UTF_8).size.toLong(),
        statusCode = response.status.value,
        statusMessage = response.status.description,
        duration = duration,
        timestamp = System.currentTimeMillis(),
        success = response.status.value in 200..299,
        exception = null,
        protocol = response.version.toString()
    )

    GlobalScope.launch(Dispatchers.IO) {
        try {
            LoggerManager.getNetworkRepository().addCall(networkCall)
        } catch (e: Exception) {
            android.util.Log.e("DevToolPlugin", "Error logging Ktor call to database: ${e.message}", e)
        }
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
private fun MockPlan.toKtorMock(): MockResponse {
    val builder = io.ktor.http.HeadersBuilder()
    headers.forEach { (key, value) -> builder.append(key, value) }
    builder.append("X-Mock-Source", "Scenario")
    builder.append("X-Mock-Scenario", scenarioName)
    builder.append("X-Mock-Key", scenarioKey)
    val code = statusCode.coerceIn(100, 599)
    return MockResponse(
        status = HttpStatusCode.fromValue(code),
        headers = builder.build(),
        body = body
    )
}