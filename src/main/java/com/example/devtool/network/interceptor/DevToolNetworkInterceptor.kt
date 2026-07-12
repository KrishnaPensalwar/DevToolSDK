package com.example.devtool.network.interceptor

import com.example.devtool.core.DevTool
import com.example.devtool.core.logging.LoggerManager
import com.example.devtool.network.parser.NetworkParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class DevToolNetworkInterceptor : Interceptor {
    private val scope = CoroutineScope(Dispatchers.IO)

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBodyString = NetworkParser.getRequestBodyString(request)
        val startTime = System.nanoTime()

        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            // TODO: Log exception in NetworkCall
            throw e
        }

        val duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
        val networkCall = NetworkParser.parseResponse(
            response = response,
            duration = duration,
            requestBodyString = requestBodyString,
            sensitiveHeaders = DevTool.config.sensitiveHeaders
        )
        
        scope.launch {
            LoggerManager.getNetworkRepository().addCall(networkCall)
        }

        return response
    }
}
