package com.example.devtool.core.collector

import com.example.devtool.core.logging.DevLog
import com.example.devtool.core.logging.LogLevel
import com.example.devtool.core.logging.LoggerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object CrashCollector : Thread.UncaughtExceptionHandler {
    private var defaultHandler: Thread.UncaughtExceptionHandler? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun start() {
        if (Thread.getDefaultUncaughtExceptionHandler() is CrashCollector) return
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        val message = "Crash in thread ${t.name}: ${e.localizedMessage}\n${e.stackTraceToString()}"
        val log = DevLog(
            tag = "CRASH",
            level = LogLevel.CRASH,
            message = message
        )
        
        // Using runBlocking or similar here might be tricky, but we want to ensure it's saved.
        // For a crash, we might want to save directly to Room synchronously or 
        // use a small delay before passing to default handler.
        scope.launch {
            LoggerManager.getRepository().add(log)
        }.invokeOnCompletion {
            defaultHandler?.uncaughtException(t, e)
        }
    }
}
