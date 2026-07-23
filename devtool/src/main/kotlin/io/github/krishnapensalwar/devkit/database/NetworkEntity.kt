package io.github.krishnapensalwar.devkit.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "network_logs")
data class NetworkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val method: String,
    val requestHeaders: String?,
    val requestBody: String?,
    val responseCode: Int,
    val responseHeaders: String?,
    val responseBody: String?,
    val timestamp: Long = System.currentTimeMillis()
)