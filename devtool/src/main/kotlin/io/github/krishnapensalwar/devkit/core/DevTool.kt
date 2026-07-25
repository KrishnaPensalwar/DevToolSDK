package io.github.krishnapensalwar.devkit.core

import android.app.Application
import android.content.Context
import io.github.krishnapensalwar.devkit.core.collector.CrashCollector
import io.github.krishnapensalwar.devkit.core.collector.PerformanceCollector
import io.github.krishnapensalwar.devkit.core.logging.LoggerManager
import io.github.krishnapensalwar.devkit.ui.floating.FloatingButtonManager

object DevTool {
    var config: DevtoolConfig =
        DevtoolConfig()
        private set

    fun init(context: Context, config: DevtoolConfig = DevtoolConfig()) {
        this.config = config
        LoggerManager.init(context)

        if (config.isCrashReportingEnabled) {
            CrashCollector.start()
        }

        if (config.isPerformanceMonitoringEnabled) {
            PerformanceCollector.start(context)
        }

        if (config.isFloatingButtonEnabled && context is Application) {
            FloatingButtonManager.init(context)
        }

    }
}
