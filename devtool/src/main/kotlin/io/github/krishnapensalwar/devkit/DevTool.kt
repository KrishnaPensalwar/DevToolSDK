package io.github.krishnapensalwar.devkit

import android.app.Application
import android.content.Context
import io.github.krishnapensalwar.devkit.core.collector.CrashCollector
import io.github.krishnapensalwar.devkit.core.collector.PerformanceCollector
import io.github.krishnapensalwar.devkit.core.logging.LoggerManager
import io.github.krishnapensalwar.devkit.ui.floating.FloatingButtonManager
import io.github.krishnapensalwar.devkit.core.DevtoolConfig

object DevTool {
    var config: DevtoolConfig = DevtoolConfig()
        private set

    /**
     * Single entry point to initialize the DevTool SDK.
     * Initializes Logging, Crash Reporting, Performance Monitoring, 
     * Network Mocking, and Database systems.
     */
    fun init(context: Context, config: DevtoolConfig = DevtoolConfig()) {
        this.config = config
        val application = context.applicationContext as Application
        
        // Initialize Logger
        LoggerManager.init(context)

        // Initialize Network, DB, and Mocking
        DevToolSdk.initialize(application)

        if (config.isCrashReportingEnabled) {
            CrashCollector.start()
        }

        if (config.isPerformanceMonitoringEnabled) {
            PerformanceCollector.start(context)
        }

        if (config.isFloatingButtonEnabled) {
            FloatingButtonManager.init(application)
        }
    }
}
