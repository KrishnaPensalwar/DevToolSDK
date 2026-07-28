package io.github.krishnapensalwar.devkit.core

data class DevtoolConfig(
    val isCrashReportingEnabled: Boolean = true,
    val isNetworkMonitoringEnabled: Boolean = true,
    val isPerformanceMonitoringEnabled: Boolean = true,
    val isFloatingButtonEnabled: Boolean = true,
    val sensitiveHeaders: Set<String> = setOf( "Cookie", "X-Api-Key")
)