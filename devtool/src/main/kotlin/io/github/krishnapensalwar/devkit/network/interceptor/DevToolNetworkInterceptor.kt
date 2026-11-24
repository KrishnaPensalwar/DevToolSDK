package io.github.krishnapensalwar.devkit.network.interceptor

import android.util.Log
import io.github.krishnapensalwar.devkit.DevTool
import io.github.krishnapensalwar.devkit.cache.CacheManager
import io.github.krishnapensalwar.devkit.core.logging.LoggerManager
import io.github.krishnapensalwar.devkit.mock.MockManager
import io.github.krishnapensalwar.devkit.mock.MockResolution
import io.github.krishnapensalwar.devkit.mock.scenario.MockPlan
import io.github.krishnapensalwar.devkit.network.model.NetworkCall
import io.github.krishnapensalwar.devkit.network.parser.NetworkParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * OkHttp [Interceptor] that logs HTTP traffic, redacts sensitive headers,
 * auto-caches responses, and short-circuits requests with mock data when mocking is enabled.
 */
class DevToolNetworkInterceptor : Interceptor {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val TAG = "NetworkInterceptor"

    /**
     * Intercepts an outgoing OkHttp request chain to inspect data or serve mock responses.
     *
     * @param chain OkHttp interceptor chain.
     * @return Real or mocked [Response].
     * @throws IOException If network execution fails.
     */
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBodyString = NetworkParser.getRequestBodyString(request)
        val startTime = System.nanoTime()

        var resolvedResponse: Response? = null
        var isMocked = false

        if (MockManager.isMockingEnabled()) {
            when (val resolution = MockManager.resolve(request, requestBodyString)) {
                MockResolution.PassThrough -> Unit
                MockResolution.Missing -> {
                    val path = request.url.encodedPath
                    val errorJson = JSONObject().apply {
                        put("error", "DevToolSDK Mocking Enabled")
                        put(
                            "message",
                            "Mocking is enabled in DevTool SDK, but no response has been cached or configured for this endpoint: $path. Please disable mocking or record/configure a mock response."
                        )
                        put("url", request.url.toString())
                    }.toString()

                    resolvedResponse = Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(404)
                        .message("Mock Not Found")
                        .body(errorJson.toResponseBody("application/json".toMediaTypeOrNull()))
                        .addHeader("Content-Type", "application/json")
                        .addHeader("X-Mock-Source", "Missing")
                        .build()
                    isMocked = true
                }
                is MockResolution.Failure -> {
                    sleepQuietly(resolution.plan.delayMs)
                    val duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
                    persistCall(buildFailureCall(request, requestBodyString, duration, resolution.plan, resolution.exception))
                    throw resolution.exception
                }
                is MockResolution.Http -> {
                    sleepQuietly(resolution.plan.delayMs)
                    resolvedResponse = resolution.response
                    isMocked = true
                }
            }
        }

        val response = resolvedResponse ?: try {
            chain.proceed(request)
        } catch (e: IOException) {
            if (!isCancellation(chain, e)) {
                Log.e(TAG, "Network exception: ${e.message}", e)
            }
            throw e
        }

        val duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)

        if (!isMocked && response.isSuccessful) {
            try {
                val responseBodyCopy = response.peekBody(Long.MAX_VALUE).string()
                val headersJson = JSONObject().apply {
                    response.headers.names().forEach { name ->
                        put(name, response.headers.values(name).joinToString(","))
                    }
                }.toString()
                val identity = MockManager.identityFor(request, requestBodyString)
                scope.launch {
                    try {
                        if (identity.isGraphQl) {
                            io.github.krishnapensalwar.devkit.mock.scenario.MockScenarioRepository.saveSnapshotIfAbsent(
                                identity = identity,
                                status = response.code,
                                headersJson = headersJson,
                                body = responseBodyCopy
                            )
                        }
                        CacheManager.saveWithHeadersJsonIfAbsent(
                            identity = identity,
                            status = response.code,
                            headersJson = headersJson,
                            body = responseBodyCopy
                        )
                    } catch (cacheErr: Exception) {
                        Log.e(TAG, "Error caching response: ${cacheErr.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to extract response for cache: ${e.message}")
            }
        }

        persistCall(
            NetworkParser.parseResponse(
                response = response,
                duration = duration,
                requestBodyString = requestBodyString,
                sensitiveHeaders = DevTool.config.sensitiveHeaders
            )
        )
        return response
    }

    private fun isCancellation(chain: Interceptor.Chain, error: IOException): Boolean {
        if (chain.call().isCanceled()) return true
        return error.message.equals("Canceled", ignoreCase = true)
    }

    private fun persistCall(networkCall: NetworkCall) {
        scope.launch {
            LoggerManager.getNetworkRepository().addCall(networkCall)
        }
    }

    private fun sleepQuietly(delayMs: Long) {
        if (delayMs <= 0) return
        try {
            Thread.sleep(delayMs)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    private fun buildFailureCall(
        request: okhttp3.Request,
        requestBodyString: String?,
        duration: Long,
        plan: MockPlan,
        exception: IOException
    ): NetworkCall {
        return NetworkCall(
            url = request.url.toString(),
            endpoint = request.url.encodedPath,
            host = request.url.host,
            method = request.method,
            requestHeaders = emptyMap(),
            requestBody = requestBodyString,
            requestSize = request.body?.contentLength() ?: 0L,
            responseHeaders = mapOf(
                "X-Mock-Source" to "Scenario",
                "X-Mock-Scenario" to plan.scenarioName,
                "X-Mock-Key" to plan.scenarioKey
            ),
            responseBody = null,
            responseSize = 0,
            statusCode = 0,
            statusMessage = plan.scenarioName,
            duration = duration,
            timestamp = System.currentTimeMillis(),
            success = false,
            exception = exception.javaClass.simpleName + ": " + exception.message,
            protocol = "MOCK"
        )
    }
}
