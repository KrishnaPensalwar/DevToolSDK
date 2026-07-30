package io.github.krishnapensalwar.devkit.internal.database

import androidx.room.Entity

@Entity(tableName = "cached_responses", primaryKeys = ["url", "method"])
internal data class CachedResponseEntity(
    val url: String,
    val method: String,
    val status: Int,
    val headersJson: String,
    val body: String
)
