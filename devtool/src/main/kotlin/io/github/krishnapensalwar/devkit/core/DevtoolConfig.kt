package io.github.krishnapensalwar.devkit.core

/**
 * Configuration options for customizing DevTool SDK behavior.
 *
 * @property isCrashReportingEnabled Whether uncaught exception crash logging is enabled. Defaults to `true`.
 * @property isNetworkMonitoringEnabled Whether network request inspection and mocking are enabled. Defaults to `true`.
 * @property isFloatingButtonEnabled Whether the draggable floating debug button overlay is enabled across activities. Defaults to `true`.
 * @property sensitiveHeaders Set of HTTP header names (case-insensitive) to redact in network logs and UI displays.
 */
data class DevtoolConfig(
    val isCrashReportingEnabled: Boolean = true,
    val isNetworkMonitoringEnabled: Boolean = true,
//  val isPerformanceMonitoringEnabled: Boolean = true,
    val isFloatingButtonEnabled: Boolean = true,
    val sensitiveHeaders: Set<String> = setOf("Cookie", "X-Api-Key")
)