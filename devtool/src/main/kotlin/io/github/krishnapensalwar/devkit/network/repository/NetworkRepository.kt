package io.github.krishnapensalwar.devkit.network.repository

import android.util.Log
import io.github.krishnapensalwar.devkit.network.database.NetworkDao
import io.github.krishnapensalwar.devkit.internal.database.NetworkEntity
import io.github.krishnapensalwar.devkit.network.model.NetworkCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject

internal class NetworkRepository(private val networkDao: NetworkDao) {
    private val _calls = MutableStateFlow<List<NetworkCall>>(emptyList())
    val calls: StateFlow<List<NetworkCall>> = _calls.asStateFlow()

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        repositoryScope.launch {
            networkDao.getAllCalls().collectLatest { entities ->
                _calls.value = entities.map { it.toDomain() }
            }
        }
    }

    suspend fun addCall(call: NetworkCall) {
        try {
            networkDao.insert(call.toEntity())
        } catch (e: Exception) {
            Log.e("NetworkRepository", "Failed to insert network call: ${e.message}", e)
        }
    }

    suspend fun clearAll() {
        networkDao.deleteAll()
    }

    private fun NetworkEntity.toDomain(): NetworkCall {
        return NetworkCall(
            id = id,
            url = url,
            endpoint = endpoint,
            host = host,
            method = method,
            requestHeaders = jsonToMap(requestHeaders),
            requestBody = requestBody,
            requestSize = requestSize,
            responseHeaders = jsonToMap(responseHeaders),
            responseBody = responseBody,
            responseSize = responseSize,
            statusCode = statusCode,
            statusMessage = statusMessage,
            duration = duration,
            timestamp = timestamp,
            success = success,
            exception = exception,
            protocol = protocol
        )
    }

    private fun NetworkCall.toEntity(): NetworkEntity {
        return NetworkEntity(
            url = url,
            endpoint = endpoint,
            host = host,
            method = method,
            requestHeaders = mapToJson(requestHeaders),
            requestBody = requestBody,
            requestSize = requestSize,
            responseHeaders = mapToJson(responseHeaders),
            responseBody = responseBody,
            responseSize = responseSize,
            statusCode = statusCode,
            statusMessage = statusMessage,
            duration = duration,
            timestamp = timestamp,
            success = success,
            exception = exception,
            protocol = protocol
        )
    }

    private fun mapToJson(map: Map<String, String>): String = JSONObject(map).toString()

    private fun jsonToMap(json: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            val jsonObject = JSONObject(json)
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = jsonObject.getString(key)
            }
        } catch (e: Exception) {
            Log.e("NetworkRepository", "Error parsing headers JSON: ${e.message}", e)
        }
        return map
    }
}
