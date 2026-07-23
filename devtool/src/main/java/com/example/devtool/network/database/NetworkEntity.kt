package com.example.devtool.network.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "network_calls")
data class NetworkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val endpoint: String,
    val host: String,
    val method: String,
    val requestHeaders: String, // JSON String
    val requestBody: String?,
    val requestSize: Long,
    val responseHeaders: String, // JSON String
    val responseBody: String?,
    val responseSize: Long,
    val statusCode: Int,
    val statusMessage: String,
    val duration: Long,
    val timestamp: Long,
    val success: Boolean,
    val exception: String?,
    val protocol: String
)
