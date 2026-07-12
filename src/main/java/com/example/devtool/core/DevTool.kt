package com.example.devtool.core

import android.content.Context
import com.example.devtool.core.collector.CrashCollector
import com.example.devtool.core.collector.LogcatCollector
import com.example.devtool.core.collector.PerformanceCollector
import com.example.devtool.core.logging.LoggerManager

object DevTool {
    fun init(context: Context, config: DevtoolConfig = DevtoolConfig()) {
        LoggerManager.init(context)

        if (config.isLogcatEnabled) {
            LogcatCollector.start()
        }

        if (config.isCrashReportingEnabled) {
            CrashCollector.start()
        }

        if (config.isPerformanceMonitoringEnabled) {
            PerformanceCollector.start(context)
        }
    }
}
