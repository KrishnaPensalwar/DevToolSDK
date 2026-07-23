package io.github.krishnapensalwar.devkit.core.collector

import android.app.ActivityManager
import android.content.Context
import io.github.krishnapensalwar.devkit.core.logging.DevLog
import io.github.krishnapensalwar.devkit.core.logging.LogLevel
import io.github.krishnapensalwar.devkit.core.logging.LoggerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object PerformanceCollector {
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun start(context: Context) {
        if (job?.isActive == true) return

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()

        job = scope.launch {
            while (true) {
                activityManager.getMemoryInfo(memoryInfo)
                val availableMegs = memoryInfo.availMem / 1048576L
                val totalMegs = memoryInfo.totalMem / 1048576L
                val percentAvail = memoryInfo.availMem.toDouble() / memoryInfo.totalMem.toDouble() * 100.0

                val message = "Memory: $availableMegs MB available of $totalMegs MB (${String.format("%.2f", percentAvail)}%)"
                
                val log = DevLog(
                    tag = "PERFORMANCE",
                    level = LogLevel.PERFORMANCE,
                    message = message
                )
                LoggerManager.getRepository().add(log)
                
                delay(30000) // Every 30 seconds
            }
        }
    }

    fun stop() {
        job?.cancel()
    }
}