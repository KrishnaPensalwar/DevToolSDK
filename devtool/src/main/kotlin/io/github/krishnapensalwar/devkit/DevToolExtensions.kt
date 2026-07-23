package io.github.krishnapensalwar.devkit

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.request.HttpRequestBuilder

/** Installs the DevTool plugin on an HttpClient config. */
fun HttpClientConfig<*>.withDevTool(configure: DevToolConfig.() -> Unit = {}) {
    install(DevToolPlugin, configure)
}

/** Enable mocking at runtime on a client that already has DevToolPlugin installed. */
fun HttpClient.enableMocking(
    resolver: (HttpRequestBuilder) -> MockResponse? = { null }
) {
    DevToolSdk.register(this)
    DevToolSdk.setMockResolver(resolver)
    DevToolSdk.setMockingEnabled(true)
}

/** Disable mocking so subsequent requests go to the real server. */
fun HttpClient.disableMocking() {
    DevToolSdk.setMockingEnabled(false)
}
fun HttpClient.enableMockingSwitch() {
    DevToolSdk.setMockingEnabled(true)
}

fun HttpClient.disableMockingSwitch() {
    DevToolSdk.setMockingEnabled(false)
}