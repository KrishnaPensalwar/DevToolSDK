package io.github.krishnapensalwar.devkit.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity that stores a cached network response.
 * Each row is uniquely identified by the combination of [url] and [method].
 *
 * @property url Full request URL.
 * @property method HTTP request method (e.g. GET, POST).
 * @property status HTTP status code of the response.
 * @property headersJson JSON-serialized map of HTTP response headers.
 * @property body String payload of the cached response body.
 * @property timestamp Unix epoch timestamp in milliseconds when response was cached.
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