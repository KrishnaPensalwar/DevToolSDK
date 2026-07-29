package io.github.krishnapensalwar.devkit.internal.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "network_calls")
internal data class NetworkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val method: String,
    val statusCode: Int,
    val requestHeadersJson: String,
    val responseHeadersJson: String,
    val requestBody: String?,
    val responseBody: String?,
    val duration: Long,
    val timestamp: Long,
    val protocol: String,
    val success: Boolean,
    val exception: String? = null
)
