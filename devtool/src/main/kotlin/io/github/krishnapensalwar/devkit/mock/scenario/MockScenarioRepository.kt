package io.github.krishnapensalwar.devkit.mock.scenario

import io.github.krishnapensalwar.devkit.internal.database.DevToolDatabase
import io.github.krishnapensalwar.devkit.internal.database.MockApiConfigEntity
import io.github.krishnapensalwar.devkit.internal.database.MockCustomScenarioEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import org.json.JSONObject

internal data class MockApiUiState(
    val enabled: Boolean = true,
    val activeScenarioKey: String? = null,
    val slowDelayMs: Long = 1_000L,
    val customScenarios: List<CustomScenarioData> = emptyList()
)

internal object MockScenarioRepository {
    private var database: DevToolDatabase? = null

    fun init(db: DevToolDatabase) {
        database = db
    }

    private fun dao() = database?.mockScenarioDao()

    fun observe(url: String, method: String): Flow<MockApiUiState> {
        val dao = dao() ?: return flowOf(MockApiUiState())
        return combine(
            dao.observeConfig(url, method),
            dao.observeCustom(url, method)
        ) { config, customs ->
            MockApiUiState(
                enabled = config?.enabled ?: true,
                activeScenarioKey = config?.activeScenarioKey,
                slowDelayMs = config?.slowDelayMs ?: 1_000L,
                customScenarios = customs.map { it.toData() }
            )
        }
    }

    suspend fun getConfig(url: String, method: String): MockApiConfigEntity? =
        dao()?.getConfig(url, method)

    suspend fun listCustom(url: String, method: String): List<CustomScenarioData> =
        dao()?.listCustom(url, method)?.map { it.toData() }.orEmpty()

    suspend fun getCustom(id: Long): CustomScenarioData? =
        dao()?.getCustom(id)?.toData()

    suspend fun setApiEnabled(url: String, method: String, enabled: Boolean) {
        val dao = dao() ?: return
        val existing = dao.getConfig(url, method)
        dao.upsertConfig(
            (existing ?: MockApiConfigEntity(url = url, method = method)).copy(enabled = enabled)
        )
    }

    suspend fun setActiveScenario(url: String, method: String, key: String?) {
        val dao = dao() ?: return
        val existing = dao.getConfig(url, method)
        dao.upsertConfig(
            (existing ?: MockApiConfigEntity(url = url, method = method)).copy(
                enabled = existing?.enabled ?: true,
                activeScenarioKey = key
            )
        )
    }

    suspend fun activateScenario(url: String, method: String, key: String) {
        val dao = dao() ?: return
        val existing = dao.getConfig(url, method)
        val nextKey = MockScenarioEngine.activateExclusive(existing?.activeScenarioKey, key)
        dao.upsertConfig(
            (existing ?: MockApiConfigEntity(url = url, method = method)).copy(
                enabled = true,
                activeScenarioKey = nextKey
            )
        )
    }

    suspend fun setSlowDelay(url: String, method: String, delayMs: Long) {
        val dao = dao() ?: return
        val existing = dao.getConfig(url, method)
        dao.upsertConfig(
            (existing ?: MockApiConfigEntity(url = url, method = method)).copy(
                slowDelayMs = delayMs.coerceAtLeast(0L)
            )
        )
    }

    suspend fun insertCustom(
        url: String,
        method: String,
        name: String,
        description: String,
        statusCode: Int,
        body: String,
        headers: Map<String, String>,
        delayMs: Long
    ): Long {
        val dao = dao() ?: return -1
        return dao.insertCustom(
            MockCustomScenarioEntity(
                url = url,
                method = method,
                name = name,
                description = description,
                statusCode = statusCode,
                body = body,
                headersJson = headersToJson(headers),
                delayMs = delayMs
            )
        )
    }

    suspend fun updateCustom(data: CustomScenarioData, url: String, method: String) {
        val dao = dao() ?: return
        dao.updateCustom(
            MockCustomScenarioEntity(
                id = data.id,
                url = url,
                method = method,
                name = data.name,
                description = data.description,
                statusCode = data.statusCode,
                body = data.body,
                headersJson = headersToJson(data.headers),
                delayMs = data.delayMs
            )
        )
    }

    suspend fun deleteCustom(id: Long, url: String, method: String) {
        val dao = dao() ?: return
        val config = dao.getConfig(url, method)
        if (config?.activeScenarioKey == customKey(id)) {
            dao.upsertConfig(config.copy(activeScenarioKey = null))
        }
        dao.deleteCustom(id)
    }

    private fun MockCustomScenarioEntity.toData(): CustomScenarioData = CustomScenarioData(
        id = id,
        name = name,
        description = description,
        statusCode = statusCode,
        body = body,
        headers = jsonToHeaders(headersJson),
        delayMs = delayMs
    )

    internal fun headersToJson(headers: Map<String, String>): String {
        val json = JSONObject()
        headers.forEach { (k, v) -> json.put(k, v) }
        return json.toString()
    }

    internal fun jsonToHeaders(json: String): Map<String, String> {
        if (json.isBlank()) return emptyMap()
        return try {
            val obj = JSONObject(json)
            buildMap {
                obj.keys().forEach { key -> put(key, obj.getString(key)) }
            }
        } catch (_: Exception) {
            emptyMap()
        }
    }
}
