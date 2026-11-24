package io.github.krishnapensalwar.devkit.network.model

data class NetworkCall(
    val id: Long = 0,
    val url: String,
    val endpoint: String,
    val host: String,
    val method: String,
    val requestHeaders: Map<String, String>,
    val requestBody: String?,
    val requestSize: Long,
    val responseHeaders: Map<String, String>,
    val responseBody: String?,
    val responseSize: Long,
    val statusCode: Int,
    val statusMessage: String,
    val duration: Long,
    val timestamp: Long,
    val success: Boolean,
    val exception: String?,
    val protocol: String
)

private fun NetworkCall.responseHeader(name: String): String? =
    responseHeaders.entries.firstOrNull { it.key.equals(name, ignoreCase = true) }?.value

/** Set by the interceptor only on responses DevTool served itself, so it reflects how this call was actually answered. */
val NetworkCall.mockSource: String? get() = responseHeader("X-Mock-Source")

val NetworkCall.mockScenarioName: String? get() = responseHeader("X-Mock-Scenario")

val NetworkCall.isMocked: Boolean
    get() = mockSource != null || exception?.contains("DevTool mock") == true