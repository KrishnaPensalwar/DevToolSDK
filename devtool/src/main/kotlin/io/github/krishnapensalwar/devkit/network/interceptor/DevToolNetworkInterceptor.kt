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
 * OkHttp [Interceptor] that logs HTTP traffic, records performance timing, redacts sensitive headers,
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
        Log.d(TAG, "[DevToolNetworkInterceptor] Request arrived: Method=${request.method}, URL=${request.url}, Connection=${chain.connection()}")

        Log.d(TAG, "[DevToolNetworkInterceptor] Extracting request body details...")
        val requestBodyString = NetworkParser.getRequestBodyString(request)
        Log.d(TAG, "[DevToolNetworkInterceptor] Extracted request body: $requestBodyString")
        val startTime = System.nanoTime()

        var resolvedResponse: Response? = null
        var isMocked = false
        var mockPlan: MockPlan? = null

        if (MockManager.isMockingEnabled()) {
            Log.d(TAG, "[DevToolNetworkInterceptor] Mocking is enabled. Resolving mock for URL: ${request.url}")
            when (val resolution = MockManager.resolve(request)) {
                MockResolution.PassThrough -> {
                    Log.d(TAG, "[DevToolNetworkInterceptor] API-level mock disabled. Passing through to network.")
                }
                MockResolution.Missing -> {
                    Log.d(TAG, "[DevToolNetworkInterceptor] No mock response found for this URL in database/cache. Returning detailed error response.")
                    val path = request.url.encodedPath
                    val errorJson = JSONObject().apply {
                        put("error", "DevToolSDK Mocking Enabled")
                        put("message", "Mocking is enabled in DevTool SDK, but no response has been cached or configured for this endpoint: $path. Please disable mocking or record/configure a mock response.")
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
                    mockPlan = resolution.plan
                    sleepQuietly(resolution.plan.delayMs)
                    val duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
                    persistCall(buildFailureCall(request, requestBodyString, duration, resolution.plan, resolution.exception))
                    throw resolution.exception
                }
                is MockResolution.Http -> {
                    mockPlan = resolution.plan
                    sleepQuietly(resolution.plan.delayMs)
                    resolvedResponse = resolution.response
                    isMocked = true
                    Log.d(TAG, "[DevToolNetworkInterceptor] Mock scenario ${resolution.plan.scenarioName}. SHORT-CIRCUITING network call.")
                }
            }
        }

        val response = resolvedResponse ?: try {
            Log.d(TAG, "[DevToolNetworkInterceptor] Proceeding network chain to: ${request.url.host}")
            chain.proceed(request)
        } catch (e: Exception) {
            Log.e(TAG, "[DevToolNetworkInterceptor] Network exception encountered: ${e.message}", e)
            throw e
        }

        val duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
        Log.d(TAG, "[DevToolNetworkInterceptor] Response received in ${duration}ms. Code=${response.code}, Message=${response.message}, Mocked=$isMocked")

        if (MockManager.isMockingEnabled() && !isMocked && response.isSuccessful) {
            try {
                val responseBodyCopy = response.peekBody(Long.MAX_VALUE).string()
                val headersJson = JSONObject().apply {
                    response.headers.names().forEach { name ->
                        put(name, response.headers.values(name).joinToString(","))
                    }
                }.toString()
                scope.launch {
                    try {
                        CacheManager.saveWithHeadersJson(
                            url = request.url.toString(),
                            method = request.method,
                            status = response.code,
                            headersJson = headersJson,
                            body = responseBodyCopy
                        )
                        Log.d(TAG, "[DevToolNetworkInterceptor] Saved response to cache database successfully.")
                    } catch (cacheErr: Exception) {
                        Log.e(TAG, "[DevToolNetworkInterceptor] Error inserting response to cache: ${cacheErr.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "[DevToolNetworkInterceptor] Failed to extract response data for cache: ${e.message}")
            }
        }

        Log.d(TAG, "[DevToolNetworkInterceptor] Parsing complete response and request context into NetworkCall model...")
        val networkCall = NetworkParser.parseResponse(
            response = response,
            duration = duration,
            requestBodyString = requestBodyString,
            sensitiveHeaders = DevTool.config.sensitiveHeaders
        )

        persistCall(networkCall)
        return response
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
