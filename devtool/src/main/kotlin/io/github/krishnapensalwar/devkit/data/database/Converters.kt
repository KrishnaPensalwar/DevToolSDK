package io.github.krishnapensalwar.devkit.data.database

import androidx.room.TypeConverter
import io.github.krishnapensalwar.devkit.core.logging.LogLevel

class Converters {
    @TypeConverter
    fun fromLogLevel(level: LogLevel): String = level.name

    @TypeConverter
    fun toLogLevel(level: String): LogLevel = LogLevel.valueOf(level)
}