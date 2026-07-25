package io.github.krishnapensalwar.devkit.network.interceptor

import android.util.Log
import io.github.krishnapensalwar.devkit.core.DevTool
import io.github.krishnapensalwar.devkit.core.logging.LoggerManager
import io.github.krishnapensalwar.devkit.network.parser.NetworkParser
import io.github.krishnapensalwar.devkit.mock.MockManager
import io.github.krishnapensalwar.devkit.cache.CacheManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class DevToolNetworkInterceptor : Interceptor {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val TAG = "NetworkInterceptor"

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

        // Check if Mocking is enabled and attempt to resolve from MockManager (DB mocks or cached responses)
        if (MockManager.isMockingEnabled()) {
            Log.d(TAG, "[DevToolNetworkInterceptor] Mocking is enabled. Resolving mock for URL: ${request.url}")
            resolvedResponse = MockManager.resolveOkHttp(request)
            if (resolvedResponse != null) {
                isMocked = true
                Log.d(TAG, "[DevToolNetworkInterceptor] Mock response FOUND in database/cache. SHORT-CIRCUITING network call.")
            } else {
                Log.d(TAG, "[DevToolNetworkInterceptor] No mock response found for this URL in database/cache.")
            }
        }

        val response: Response
        if (resolvedResponse != null) {
            response = resolvedResponse
        } else {
            try {
                Log.d(TAG, "[DevToolNetworkInterceptor] Proceeding network chain to: ${request.url.host}")
                response = chain.proceed(request)
            } catch (e: Exception) {
                Log.e(TAG, "[DevToolNetworkInterceptor] Network exception encountered: ${e.message}", e)
                // TODO: Log exception in NetworkCall
                throw e
            }
        }

        val duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
        Log.d(TAG, "[DevToolNetworkInterceptor] Response received in ${duration}ms. Code=${response.code}, Message=${response.message}, Mocked=$isMocked")

        // If mocking is enabled and response is from real network and successful, cache it for future mock usage
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
        
        Log.d(TAG, "[DevToolNetworkInterceptor] NetworkCall Data Generated:\n" +
                "  URL: ${networkCall.url}\n" +
                "  Method: ${networkCall.method}\n" +
                "  Status Code: ${networkCall.statusCode}\n" +
                "  Duration: ${networkCall.duration}ms\n" +
                "  Protocol: ${networkCall.protocol}\n" +
                "  Success: ${networkCall.success}")
        
        Log.d(TAG, "[DevToolNetworkInterceptor] Launching coroutine job (Dispatchers.IO) to persist NetworkCall data into DB.")
        scope.launch {
            LoggerManager.getNetworkRepository().addCall(networkCall)
        }

        return response
    }
}