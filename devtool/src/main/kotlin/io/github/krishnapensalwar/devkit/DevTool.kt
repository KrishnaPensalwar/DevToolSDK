package io.github.krishnapensalwar.devkit

import android.app.Application
import android.content.Context
import io.github.krishnapensalwar.devkit.core.collector.CrashCollector
import io.github.krishnapensalwar.devkit.core.collector.PerformanceCollector
import io.github.krishnapensalwar.devkit.core.logging.LoggerManager
import io.github.krishnapensalwar.devkit.ui.floating.FloatingButtonManager

/**
 * Main entry point for the DevTool SDK.
 *
 * Provides single-call initialization for network inspection, logging, crash reporting,
 * database/storage inspection, and API mocking.
 */
object DevTool {
    /**
     * Active configuration for the DevTool SDK.
     */
    var config: DevtoolConfig = DevtoolConfig()
        private set


    @Volatile
    private var initialized = false
    /**
     * Single entry point to initialize the DevTool SDK.
     *
     * Initializes logging, crash reporting, network mocking, and storage inspection.
     * Call once from [Application.onCreate] or an Activity context.
     *
     * @param context Application or Activity context used for initialization.
     * @param config Optional configuration settings to customize SDK features.
     */
    fun init(context: Context, config: DevtoolConfig = DevtoolConfig()) {
        if(initialized){
           return
        }
        synchronized(this){
            if(initialized) return
            this.config = config
            val application = context.applicationContext as Application

            // Initialize Logger
            LoggerManager.init(context)

            // Initialize Network, DB, and Mocking
            DevToolSdk.initialize(application)

            if (config.isCrashReportingEnabled) {
                CrashCollector.start()
            }

//        if (config.isPerformanceMonitoringEnabled) {
//            PerformanceCollector.start(context)
//        }

            if (config.isFloatingButtonEnabled) {
                FloatingButtonManager.init(application)
            }

            initialized = true
        }

    }
}
