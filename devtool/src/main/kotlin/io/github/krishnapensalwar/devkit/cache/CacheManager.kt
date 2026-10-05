package io.github.krishnapensalwar.devkit.cache

import androidx.room.RoomDatabase
import io.github.krishnapensalwar.devkit.MockResponse
import io.github.krishnapensalwar.devkit.internal.database.CachedResponseDao
import io.github.krishnapensalwar.devkit.internal.database.CachedResponseEntity
import io.github.krishnapensalwar.devkit.internal.database.DevToolDatabase
import io.github.krishnapensalwar.devkit.mock.identity.RequestIdentity
import io.ktor.http.Headers
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

internal object CacheManager {
    private lateinit var dao: CachedResponseDao

    fun init(database: RoomDatabase) {
        dao = (database as DevToolDatabase).cachedResponseDao()
    }

    suspend fun get(identity: RequestIdentity): CachedResponseEntity? =
        withContext(Dispatchers.IO) { dao.getByIdentityKey(identity.identityKey) }

    suspend fun get(url: String, method: String): CachedResponseEntity? =
        get(RequestIdentity.rest(url, method))

    suspend fun save(
        identity: RequestIdentity,
        status: Int,
        headers: Headers,
        body: String
    ) = withContext(Dispatchers.IO) {
        if (dao.getByIdentityKey(identity.identityKey) != null) return@withContext
        val headersJson = JSONObject().apply {
            headers.names().forEach { name ->
                val values = headers.getAll(name) ?: emptyList<String>()
                put(name, values.joinToString(","))
            }
        }.toString()
        dao.insert(entity(identity, status, headersJson, body))
    }

    suspend fun save(
        url: String,
        method: String,
        status: Int,
        headers: Headers,
        body: String
    ) = save(RequestIdentity.rest(url, method), status, headers, body)

    suspend fun saveWithHeadersJson(
        identity: RequestIdentity,
        status: Int,
        headersJson: String,
        body: String
    ) = withContext(Dispatchers.IO) {
        dao.insert(entity(identity, status, headersJson, body))
    }

    suspend fun saveWithHeadersJson(
        url: String,
        method: String,
        status: Int,
        headersJson: String,
        body: String
    ) = saveWithHeadersJson(RequestIdentity.rest(url, method), status, headersJson, body)

    suspend fun saveWithHeadersJsonIfAbsent(
        identity: RequestIdentity,
        status: Int,
        headersJson: String,
        body: String
    ) {
        if (get(identity) != null) return
        saveWithHeadersJson(identity, status, headersJson, body)
    }

    suspend fun saveWithHeadersJsonIfAbsent(
        url: String,
        method: String,
        status: Int,
        headersJson: String,
        body: String
    ) = saveWithHeadersJsonIfAbsent(RequestIdentity.rest(url, method), status, headersJson, body)

    fun toMockResponse(entity: CachedResponseEntity): MockResponse {
        val json = JSONObject(entity.headersJson)
        val builder = HeadersBuilder()
        json.keys().forEach { key ->
            val value = json.getString(key)
            value.split(",").forEach { v ->
                builder.append(key, v)
            }
        }
        return MockResponse(
            status = HttpStatusCode.fromValue(entity.status),
            headers = builder.build(),
            body = ByteReadChannel(entity.body)
        )
    }

    private fun entity(
        identity: RequestIdentity,
        status: Int,
        headersJson: String,
        body: String
    ) = CachedResponseEntity(
        identityKey = identity.identityKey,
        url = identity.url,
        method = identity.method,
        status = status,
        headersJson = headersJson,
        body = body,
        displayName = identity.displayName,
        protocol = identity.protocol.name
    )
}
