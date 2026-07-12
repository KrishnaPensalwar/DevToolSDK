package com.example.devtool.core

data class DevtoolConfig(
    val isLogcatEnabled: Boolean = true,
    val isCrashReportingEnabled: Boolean = true,
    val isNetworkMonitoringEnabled: Boolean = true,
    val isPerformanceMonitoringEnabled: Boolean = true
)
