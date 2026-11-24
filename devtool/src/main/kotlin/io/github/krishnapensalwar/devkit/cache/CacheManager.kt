package io.github.krishnapensalwar.devkit.cache

import androidx.room.RoomDatabase
import io.github.krishnapensalwar.devkit.internal.database.CachedResponseDao
import io.github.krishnapensalwar.devkit.internal.database.CachedResponseEntity
import io.github.krishnapensalwar.devkit.internal.database.DevToolDatabase
import io.github.krishnapensalwar.devkit.mock.identity.RequestIdentity
import io.ktor.http.Headers
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
                val values = headers.getAll(name) ?: emptyList()
                put(name, values.joinToString(","))
            }
        }.toString()
        dao.insert(entity(identity, status, headersJson, body))
    }
    suspend fun saveWithHeadersJson(
        identity: RequestIdentity,
        status: Int,
        headersJson: String,
        body: String
    ) = withContext(Dispatchers.IO) {
        dao.insert(entity(identity, status, headersJson, body))
    }

    suspend fun saveWithHeadersJsonIfAbsent(
        identity: RequestIdentity,
        status: Int,
        headersJson: String,
        body: String
    ) {
        if (get(identity) != null) return
        saveWithHeadersJson(identity, status, headersJson, body)
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
