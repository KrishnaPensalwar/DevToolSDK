package io.github.krishnapensalwar.devkit.network.parser

import android.util.Log
import io.github.krishnapensalwar.devkit.network.model.NetworkCall
import okhttp3.Headers
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset

object NetworkParser {
    private const val TAG = "NetworkInterceptor"

    fun parseRequest(request: Request, sensitiveHeaders: Set<String> = emptySet()): NetworkCall {
        Log.d(TAG, "[NetworkParser] parseRequest: Processing request URL=${request.url}, Method=${request.method}")
        val requestBody = request.body
        val buffer = Buffer()
        requestBody?.writeTo(buffer)

        val bodyString = if (isPlaintext(buffer)) {
            val body = buffer.readString(Charset.forName("UTF-8"))
            Log.d(TAG, "[NetworkParser] parseRequest: Plaintext body content = \"$body\"")
            body
        } else {
            Log.d(TAG, "[NetworkParser] parseRequest: Binary or empty body. Omitted.")
            "(binary body omitted)"
        }

        val requestHeadersMap = request.headers.toMap(sensitiveHeaders)
        Log.d(TAG, "[NetworkParser] parseRequest: Extracted request headers = $requestHeadersMap")

        return NetworkCall(
            url = request.url.toString(),
            endpoint = request.url.encodedPath,
            host = request.url.host,
            method = request.method,
            requestHeaders = requestHeadersMap,
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
        Log.d(TAG, "[NetworkParser] parseResponse: Extracting response details for URL=${request.url}")
        
        Log.d(TAG, "[NetworkParser] parseResponse: Peeking response body...")
        val responseBody = response.peekBody(Long.MAX_VALUE)
        val responseBodyString = try {
            val body = responseBody.string()
            Log.d(TAG, "[NetworkParser] parseResponse: Extracted response body content = \"$body\"")
            body
        } catch (e: Exception) {
            Log.e(TAG, "[NetworkParser] parseResponse: Error reading response body stream: ${e.message}")
            "(error reading response body)"
        }

        Log.d(TAG, "[NetworkParser] parseResponse: Mapping headers...")
        val reqHeadersMap = request.headers.toMap(sensitiveHeaders)
        val resHeadersMap = response.headers.toMap(sensitiveHeaders)
        Log.d(TAG, "[NetworkParser] parseResponse: Extracted request headers: $reqHeadersMap")
        Log.d(TAG, "[NetworkParser] parseResponse: Extracted response headers: $resHeadersMap")

        val networkCall = NetworkCall(
            url = request.url.toString(),
            endpoint = request.url.encodedPath,
            host = request.url.host,
            method = request.method,
            requestHeaders = reqHeadersMap,
            requestBody = requestBodyString,
            requestSize = request.body?.contentLength() ?: 0L,
            responseHeaders = resHeadersMap,
            responseBody = responseBodyString,
            responseSize = responseBody.contentLength(),
            statusCode = response.code,
            statusMessage = response.message,
            duration = duration,
            timestamp = System.currentTimeMillis(),
            success = response.isSuccessful,
            exception = null,
            protocol = response.protocol.toString()
        )
        Log.d(TAG, "[NetworkParser] parseResponse: Successfully constructed NetworkCall.")
        return networkCall
    }

    fun getRequestBodyString(request: Request): String? {
        val requestBody = request.body ?: run {
            Log.d(TAG, "[NetworkParser] getRequestBodyString: No body present in request.")
            return null
        }
        return try {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            if (isPlaintext(buffer)) {
                val body = buffer.readString(Charset.forName("UTF-8"))
                Log.d(TAG, "[NetworkParser] getRequestBodyString: Plaintext body found (${buffer.size} bytes). Content = \"$body\"")
                body
            } else {
                Log.d(TAG, "[NetworkParser] getRequestBodyString: Binary body found (${buffer.size} bytes). Content omitted.")
                "(binary body omitted)"
            }
        } catch (e: Exception) {
            Log.e(TAG, "[NetworkParser] getRequestBodyString: Exception extracting body: ${e.message}", e)
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
                    Log.d(TAG, "[NetworkParser] isPlaintext: Control code-point '$codePoint' found. Classification=BINARY.")
                    return false
                }
            }
            Log.d(TAG, "[NetworkParser] isPlaintext: Verification complete. Classification=PLAINTEXT.")
            true
        } catch (e: IOException) {
            Log.e(TAG, "[NetworkParser] isPlaintext: IOException during check: ${e.message}")
            false
        }
    }

    private fun Headers.toMap(sensitiveHeaders: Set<String>): Map<String, String> {
        val map = mutableMapOf<String, String>()
        var obfuscatedCount = 0
        for (i in 0 until size) {
            val name = name(i)
            val isSensitive = sensitiveHeaders.any { it.equals(name, ignoreCase = true) }
            val value = if (isSensitive) {
                obfuscatedCount++
                "••••••••"
            } else {
                value(i)
            }
            map[name] = value
        }
        if (obfuscatedCount > 0) {
            Log.d(TAG, "[NetworkParser] Headers.toMap: Obfuscated $obfuscatedCount header(s). Map result = $map")
        } else {
            Log.d(TAG, "[NetworkParser] Headers.toMap: Extracted headers map = $map")
        }
        return map
    }
}