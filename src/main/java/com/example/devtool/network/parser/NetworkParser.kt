package com.example.devtool.network.parser

import com.example.devtool.network.model.NetworkCall
import okhttp3.Headers
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

object NetworkParser {
    
    fun parseRequest(request: Request, sensitiveHeaders: Set<String> = emptySet()): NetworkCall {
        val requestBody = request.body
        val buffer = Buffer()
        requestBody?.writeTo(buffer)
        
        val bodyString = if (isPlaintext(buffer)) {
            buffer.readString(Charset.forName("UTF-8"))
        } else {
            "(binary body omitted)"
        }

        return NetworkCall(
            url = request.url.toString(),
            endpoint = request.url.encodedPath,
            host = request.url.host,
            method = request.method,
            requestHeaders = request.headers.toMap(sensitiveHeaders),
            requestBody = bodyString,
            requestSize = requestBody?.contentLength() ?: 0L,
            responseHeaders = emptyMap(),
            responseBody = null,
            responseSize = 0,
            statusCode = 0,
            statusMessage = "",
            duration = 0,
            timestamp = System.currentTimeMillis(),
            success = false,
            exception = null,
            protocol = ""
        )
    }

    fun parseResponse(
        response: Response,
        duration: Long,
        requestBodyString: String?,
        sensitiveHeaders: Set<String> = emptySet()
    ): NetworkCall {
        val request = response.request
        val responseBody = response.peekBody(Long.MAX_VALUE)
        
        return NetworkCall(
            url = request.url.toString(),
            endpoint = request.url.encodedPath,
            host = request.url.host,
            method = request.method,
            requestHeaders = request.headers.toMap(sensitiveHeaders),
            requestBody = requestBodyString,
            requestSize = request.body?.contentLength() ?: 0L,
            responseHeaders = response.headers.toMap(sensitiveHeaders),
            responseBody = responseBody.string(),
            responseSize = responseBody.contentLength(),
            statusCode = response.code,
            statusMessage = response.message,
            duration = duration,
            timestamp = System.currentTimeMillis(),
            success = response.isSuccessful,
            exception = null,
            protocol = response.protocol.toString()
        )
    }

    fun getRequestBodyString(request: Request): String? {
        val requestBody = request.body ?: return null
        return try {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            if (isPlaintext(buffer)) {
                buffer.readString(Charset.forName("UTF-8"))
            } else {
                "(binary body omitted)"
            }
        } catch (e: Exception) {
            "(error reading body)"
        }
    }

    private fun isPlaintext(buffer: Buffer): Boolean {
        return try {
            val prefix = Buffer()
            val byteCount = if (buffer.size < 64) buffer.size else 64
            buffer.copyTo(prefix, 0, byteCount)
            for (i in 0 until 16) {
                if (prefix.exhausted()) break
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            true
        } catch (e: IOException) {
            false
        }
    }
    
    private fun Headers.toMap(sensitiveHeaders: Set<String>): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (i in 0 until size) {
            val name = name(i)
            val value = if (sensitiveHeaders.any { it.equals(name, ignoreCase = true) }) {
                "********"
            } else {
                value(i)
            }
            map[name] = value
        }
        return map
    }
}
