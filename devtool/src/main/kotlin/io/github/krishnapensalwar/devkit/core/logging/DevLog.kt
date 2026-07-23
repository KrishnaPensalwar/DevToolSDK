package io.github.krishnapensalwar.devkit.core.logging

data class DevLog(
    val id: Long = 0,
    val tag: String,
    val level: LogLevel,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)