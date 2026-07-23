package com.example.devtool.cache

import androidx.room.RoomDatabase
import com.example.devtool.database.CachedResponseDao
import com.example.devtool.database.CachedResponseEntity
import io.ktor.http.Headers
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteReadChannel.Companion.Empty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Singleton responsible for interacting with the cached_responses table.
 */
object CacheManager {
    private lateinit var dao: CachedResponseDao

    fun init(database: RoomDatabase) {
        dao = (database as com.example.devtool.database.DevToolDatabase).cachedResponseDao()
    }

    /** Retrieve a cached response for the given url and HTTP method. */
    suspend fun get(url: String, method: String): CachedResponseEntity? =
        withContext(Dispatchers.IO) { dao.get(url, method) }

    /** Save a response to the cache. */
    suspend fun save(
        url: String,
        method: String,
        status: Int,
        headers: Headers,
        body: String
    ) = withContext(Dispatchers.IO) {
        // Serialize headers into a JSON map (name -> comma-separated values)
        val headersJson = JSONObject().apply {
            headers.names().forEach { name ->
                val values = headers.getAll(name) ?: emptyList<String>()
                put(name, values.joinToString(","))
            }
        }.toString()
        val entity = CachedResponseEntity(
            url = url,
            method = method,
            status = status,
            headersJson = headersJson,
            body = body
        )
        dao.insert(entity)
    }

    /** Convert a CachedResponseEntity into a MockResponse used by the plugin. */
    fun toMockResponse(entity: CachedResponseEntity): com.example.devtool.MockResponse {
        // Build Headers from stored JSON (name -> comma‑separated values)
        val json = JSONObject(entity.headersJson)
        val builder = io.ktor.http.HeadersBuilder()
        json.keys().forEach { key ->
            val value = json.getString(key)
            // Split by comma to restore multiple values
            value.split(",").forEach { v ->
                builder.append(key, v)
            }
        }
        val headers = builder.build()
        return com.example.devtool.MockResponse(
            status = HttpStatusCode.fromValue(entity.status),
            headers = headers,
            body = ByteReadChannel(entity.body)
        )
    }
}
