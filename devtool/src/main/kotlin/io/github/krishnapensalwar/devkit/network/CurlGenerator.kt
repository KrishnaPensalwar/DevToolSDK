package io.github.krishnapensalwar.devkit.network

import io.github.krishnapensalwar.devkit.network.model.NetworkCall

object CurlGenerator {
    fun generateCurl(call: NetworkCall): String {
        return buildString {
            append("curl -X ${call.method} \\\n")
            append("  \"${call.url}\" \\\n")
            
            call.requestHeaders.forEach { (key, value) ->
                append("  -H \"$key: ${escape(value)}\" \\\n")
            }
            
            if (!call.requestBody.isNullOrBlank()) {
                append("  -d '${escapeBody(call.requestBody)}'")
            } else {
                // Remove trailing backslash if no data
                if (length > 2 && this[length - 2] == '\\') {
                    delete(length - 3, length)
                }
            }
        }.trim()
    }

    private fun escape(value: String): String {
        return value.replace("\"", "\\\"")
    }

    private fun escapeBody(body: String): String {
        return body.replace("'", "'\\''")
    }
}
