package io.github.krishnapensalwar.devkit

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.statement.bodyAsText
import io.github.krishnapensalwar.devkit.network.model.NetworkCall
import io.github.krishnapensalwar.devkit.core.logging.LoggerManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Installs the DevTool plugin on an [HttpClientConfig].
 *
 * @param configure Lambda block to configure [KtorDevToolConfig] options such as mock resolvers or request modifiers.
 */
fun HttpClientConfig<*>.withDevTool(configure: KtorDevToolConfig.() -> Unit = {}) {
    install(DevToolPlugin, configure)
    install(ResponseObserver) {
        onResponse { response ->
            val startTime = response.call.request.attributes.getOrNull(StartTimeKey)
            val duration = if (startTime != null) {
                java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            } else {
                0L
            }

            val request = response.call.request
            val reqHeadersMap = mutableMapOf<String, String>()
            request.headers.forEach { key, values ->
                val isSensitive = DevTool.config.sensitiveHeaders.any { it.equals(key, ignoreCase = true) }
                reqHeadersMap[key] = if (isSensitive) "••••••••" else values.joinToString(",")
            }

            val resHeadersMap = mutableMapOf<String, String>()
            response.headers.forEach { key, values ->
                val isSensitive = DevTool.config.sensitiveHeaders.any { it.equals(key, ignoreCase = true) }
                resHeadersMap[key] = if (isSensitive) "••••••••" else values.joinToString(",")
            }

            val requestBodyText = getRequestBodyString(request.content)
            
            val responseBodyText = try {
                response.bodyAsText()
            } catch (e: Exception) {
                "(error reading response body)"
            }

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
    }
}

private fun getRequestBodyString(content: io.ktor.http.content.OutgoingContent): String? {
    return when (content) {
        is io.ktor.http.content.OutgoingContent.ByteArrayContent -> {
            try {
                String(content.bytes(), Charsets.UTF_8)
            } catch (e: Exception) {
                "(error reading byte array body)"
            }
        }
        is io.ktor.http.content.OutgoingContent.NoContent -> null
        is io.ktor.http.content.OutgoingContent.WriteChannelContent -> "(write channel content omitted)"
        is io.ktor.http.content.OutgoingContent.ReadChannelContent -> "(read channel content omitted)"
        is io.ktor.http.content.OutgoingContent.ProtocolUpgrade -> "(protocol upgrade content omitted)"
        else -> null
    }
}

/**
 * Enables network mocking at runtime for a client with [DevToolPlugin] installed.
 *
 * @param resolver Optional resolver block returning a [MockResponse] for a given request builder.
 */
fun HttpClient.enableMocking(
    resolver: (HttpRequestBuilder) -> MockResponse? = { null }
) {
    DevToolSdk.register(this)
    DevToolSdk.setMockResolver(resolver)
    DevToolSdk.setMockingEnabled(true)
}

/**
 * Disables network mocking so subsequent HTTP requests reach the live backend.
 */
fun HttpClient.disableMocking() {
    DevToolSdk.setMockingEnabled(false)
}

/**
 * Toggles global network mocking state to enabled.
 */
fun HttpClient.enableMockingSwitch() {
    DevToolSdk.setMockingEnabled(true)
}

/**
 * Toggles global network mocking state to disabled.
 */
fun HttpClient.disableMockingSwitch() {
    DevToolSdk.setMockingEnabled(false)
}