package com.example.devtool.network

import com.example.devtool.core.logging.DevLog
import com.example.devtool.core.logging.LogLevel
import com.example.devtool.core.logging.LoggerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

class DevToolInterceptor : Interceptor {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.nanoTime()

        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            logNetworkError(request, e)
            throw e
        }

        val duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
        logNetworkResponse(request, response, duration)

        return response
    }

    private fun logNetworkResponse(request: okhttp3.Request, response: Response, duration: Long) {
        val message = buildString {
            appendLine("URL: ${request.url}")
            appendLine("Method: ${request.method}")
            appendLine("Status Code: ${response.code}")
            appendLine("Duration: ${duration}ms")
            appendLine("Headers: ${response.headers}")
            
            val responseBody = response.peekBody(1024 * 1024)
            appendLine("Response: ${responseBody.string()}")
        }

        val log = DevLog(
            tag = "NETWORK",
            level = LogLevel.NETWORK,
            message = message
        )
        scope.launch {
            LoggerManager.getRepository().add(log)
        }
    }

    private fun logNetworkError(request: okhttp3.Request, e: Exception) {
        val message = buildString {
            appendLine("URL: ${request.url}")
            appendLine("Method: ${request.method}")
            appendLine("Error: ${e.message}")
        }
        val log = DevLog(
            tag = "NETWORK",
            level = LogLevel.NETWORK,
            message = message
        )
        scope.launch {
            LoggerManager.getRepository().add(log)
        }
    }
}
