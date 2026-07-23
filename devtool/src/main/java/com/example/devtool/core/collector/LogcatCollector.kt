package com.example.devtool.core.collector

import com.example.devtool.core.logging.LoggerManager
import com.example.devtool.core.parser.LogParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

object LogcatCollector {
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun start() {
        if (job?.isActive == true) return

        job = scope.launch {
            try {
                // Clear logcat before starting to avoid reading old logs if preferred, 
                // or just start reading from now.
                // Runtime.getRuntime().exec("logcat -c")
                
                val process = Runtime.getRuntime().exec("logcat -v threadtime")
                val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))

                var line: String?
                while (true) {
                    line = bufferedReader.readLine() ?: break
                    val devLog = LogParser.parse(line)
                    if (devLog != null) {
                        LoggerManager.getRepository().add(devLog)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stop() {
        job?.cancel()
    }
}
