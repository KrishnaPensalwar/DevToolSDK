package com.example.devtool.network.model

data class NetworkCall(
    val id: Long = 0,
    val url: String,
    val endpoint: String,
    val host: String,
    val method: String,
    val requestHeaders: Map<String, String>,
    val requestBody: String?,
    val requestSize: Long,
    val responseHeaders: Map<String, String>,
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
