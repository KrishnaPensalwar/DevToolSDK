package io.github.krishnapensalwar.devkit.internal.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_responses")
internal data class CachedResponseEntity(
    @PrimaryKey val identityKey: String,
    val url: String,
    val method: String,
    val status: Int,
    val headersJson: String,
    val body: String,
    val displayName: String = "",
    val protocol: String = "REST"
)
