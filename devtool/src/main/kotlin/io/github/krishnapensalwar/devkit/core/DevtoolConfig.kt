package io.github.krishnapensalwar.devkit.core

data class DevtoolConfig(
    val isLogcatEnabled: Boolean = true,
    val isCrashReportingEnabled: Boolean = true,
    val isNetworkMonitoringEnabled: Boolean = true,
    val isPerformanceMonitoringEnabled: Boolean = true,
    val isFloatingButtonEnabled: Boolean = true,
    val isShakeEnabled: Boolean = true,
    val sensitiveHeaders: Set<String> = setOf("Authorization", "Cookie", "X-Api-Key")
)