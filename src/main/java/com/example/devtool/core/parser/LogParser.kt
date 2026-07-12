package com.example.devtool.core.parser

import com.example.devtool.core.logging.DevLog
import com.example.devtool.core.logging.LogLevel

object LogParser {
    fun parse(rawLog: String): DevLog? {
        // Example: 07-12 10:30:22.123 1234 5678 D LOGIN: User logged in
        val regex = """\d{2}-\d{2}\s\d{2}:\d{2}:\d{2}\.\d{3}\s+\d+\s+\d+\s([VDIWE])\s+(.*?):\s(.*)""".toRegex()
        val matchResult = regex.find(rawLog)

        return if (matchResult != null) {
            val (levelStr, tag, message) = matchResult.destructured
            DevLog(
                tag = tag.trim(),
                level = mapLogLevel(levelStr),
                message = message.trim()
            )
        } else {
            // If it doesn't match the standard format, we can still try to log it as DEBUG
            if (rawLog.isNotBlank()) {
                DevLog(tag = "Logcat", level = LogLevel.DEBUG, message = rawLog)
            } else null
        }
    }

    private fun mapLogLevel(levelStr: String): LogLevel {
        return when (levelStr) {
            "V" -> LogLevel.VERBOSE
            "D" -> LogLevel.DEBUG
            "I" -> LogLevel.INFO
            "W" -> LogLevel.WARN
            "E" -> LogLevel.ERROR
            else -> LogLevel.DEBUG
        }
    }
}
