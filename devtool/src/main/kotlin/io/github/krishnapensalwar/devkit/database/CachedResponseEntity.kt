package io.github.krishnapensalwar.devkit.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity that stores a cached network response.
 * Each row is uniquely identified by the combination of url + method.
 */
@Entity(tableName = "cached_responses", primaryKeys = ["url", "method"])
data class CachedResponseEntity(
    val url: String,
    val method: String,
    val status: Int,
    val headersJson: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis()
)