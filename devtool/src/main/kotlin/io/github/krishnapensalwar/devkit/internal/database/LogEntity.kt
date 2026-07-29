package io.github.krishnapensalwar.devkit.internal.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.krishnapensalwar.devkit.core.logging.LogLevel

@Entity(tableName = "logs")
internal data class LogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tag: String,
    val message: String,
    val level: LogLevel,
    val timestamp: Long
)
