package io.github.krishnapensalwar.devkit.internal.database

import androidx.room.TypeConverter
import io.github.krishnapensalwar.devkit.core.logging.LogLevel

internal class Converters {
    @TypeConverter
    fun fromLogLevel(level: LogLevel): String = level.name

    @TypeConverter
    fun toLogLevel(name: String): LogLevel = LogLevel.valueOf(name)
}
