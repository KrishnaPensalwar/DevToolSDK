package io.github.krishnapensalwar.devkit.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.krishnapensalwar.devkit.core.logging.LogLevel

@Entity(tableName = "logs")
data class LogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tag: String,
    val level: LogLevel,
    val message: String,
    val timestamp: Long
)