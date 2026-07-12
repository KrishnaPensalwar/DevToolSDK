package com.example.devtool.data.database

import androidx.room.TypeConverter
import com.example.devtool.core.logging.LogLevel

class Converters {
    @TypeConverter
    fun fromLogLevel(level: LogLevel): String = level.name

    @TypeConverter
    fun toLogLevel(level: String): LogLevel = LogLevel.valueOf(level)
}
