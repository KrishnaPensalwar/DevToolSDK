package io.github.krishnapensalwar.devkit

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.request.HttpRequestBuilder

/**
 * Installs the DevTool plugin on an [HttpClientConfig].
 *
 * @param configure Lambda block to configure [DevToolConfig] options such as mock resolvers or request modifiers.
 */
fun HttpClientConfig<*>.withDevTool(configure: DevToolConfig.() -> Unit = {}) {
    install(DevToolPlugin, configure)
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