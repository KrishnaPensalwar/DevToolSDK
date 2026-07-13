package com.example.devtool.core

import android.app.Application
import android.content.Context
import com.example.devtool.core.collector.CrashCollector
import com.example.devtool.core.collector.LogcatCollector
import com.example.devtool.core.collector.PerformanceCollector
import com.example.devtool.core.logging.LoggerManager
import android.content.Intent
import com.example.devtool.core.sensor.ShakeDetector
import com.example.devtool.ui.dashboard.DashboardActivity
import com.example.devtool.ui.floating.FloatingButtonManager

object DevTool {
    private var shakeDetector: ShakeDetector? = null
    var config: DevtoolConfig = DevtoolConfig()
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

//        if (config.isShakeEnabled) {
//            shakeDetector = ShakeDetector {
//                context.startActivity(DashboardActivity.newIntent(context).apply {
//                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                })
//            }
//            shakeDetector?.start(context)
//        }
    }
}
