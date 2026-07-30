package io.github.krishnapensalwar.devkit.network.repository

import android.util.Log
import io.github.krishnapensalwar.devkit.network.database.NetworkDao
import io.github.krishnapensalwar.devkit.network.database.NetworkEntity
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
    private val TAG = "NetworkInterceptor"

    init {
        repositoryScope.launch {
            Log.d(TAG, "[NetworkRepository] Registering database observer Flow...")
            networkDao.getAllCalls().collectLatest { entities ->
                Log.d(TAG, "[NetworkRepository] Database observer emitted ${entities.size} entities.")
                val domainCalls = entities.map { it.toDomain() }
                _calls.value = domainCalls
                Log.d(TAG, "[NetworkRepository] Database observer: Updated calls StateFlow list. Current items count = ${domainCalls.size}")
                if (domainCalls.isNotEmpty()) {
                    val latest = domainCalls.first()
                    Log.d(TAG, "[NetworkRepository] Latest emitted call: Method=${latest.method}, Host=${latest.host}, Code=${latest.statusCode}, Size=${latest.responseSize} bytes")
                }
            }
        }
    }

    suspend fun addCall(call: NetworkCall) {
        Log.d(TAG, "[NetworkRepository] addCall: Method=${call.method}, Endpoint=${call.endpoint}, URL=${call.url}")
        try {
            val entity = call.toEntity()
            Log.d(TAG, "[NetworkRepository] addCall: Saving NetworkEntity containing " +
                    "urlLength=${entity.url.length}, " +
                    "reqHeadersJsonLength=${entity.requestHeaders.length}, " +
                    "resHeadersJsonLength=${entity.responseHeaders.length}, " +
                    "reqBodyLength=${entity.requestBody?.length ?: 0}, " +
                    "resBodyLength=${entity.responseBody?.length ?: 0}")
            networkDao.insert(entity)
            Log.d(TAG, "[NetworkRepository] addCall: SQLite insertion completed successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "[NetworkRepository] addCall: SQLite insertion failed: ${e.message}", e)
        }
    }

    suspend fun clearAll() {
        Log.d(TAG, "[NetworkRepository] clearAll: Wiping database network_calls table...")
        networkDao.deleteAll()
        Log.d(TAG, "[NetworkRepository] clearAll: database wipe complete.")
    }

    private fun NetworkEntity.toDomain(): NetworkCall {
        val reqMap = jsonToMap(requestHeaders)
        val resMap = jsonToMap(responseHeaders)
        return NetworkCall(
            id = id,
            url = url,
            endpoint = endpoint,
            host = host,
            method = method,
            requestHeaders = reqMap,
            requestBody = requestBody,
            requestSize = requestSize,
            responseHeaders = resMap,
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
        val reqJson = mapToJson(requestHeaders)
        val resJson = mapToJson(responseHeaders)
        return NetworkEntity(
            url = url,
            endpoint = endpoint,
            host = host,
            method = method,
            requestHeaders = reqJson,
            requestBody = requestBody,
            requestSize = requestSize,
            responseHeaders = resJson,
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

    private fun mapToJson(map: Map<String, String>): String {
        val json = JSONObject(map).toString()
        Log.d(TAG, "[NetworkRepository] mapToJson: Map content = $map -> JSON string result = \"$json\"")
        return json
    }

    private fun jsonToMap(json: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            val jsonObject = JSONObject(json)
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = jsonObject.getString(key)
            }
            Log.d(TAG, "[NetworkRepository] jsonToMap: JSON string = \"$json\" -> Map content result = $map")
        } catch (e: Exception) {
            Log.e(TAG, "[NetworkRepository] jsonToMap: Error parsing JSON headers: ${e.message}", e)
        }
        return map
    }
}
