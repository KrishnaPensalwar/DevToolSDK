package io.github.krishnapensalwar.devkit.mock.scenario

import io.github.krishnapensalwar.devkit.internal.database.DevToolDatabase
import io.github.krishnapensalwar.devkit.internal.database.MockApiConfigEntity
import io.github.krishnapensalwar.devkit.internal.database.MockCustomScenarioEntity
import io.github.krishnapensalwar.devkit.mock.identity.RequestIdentity
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

    fun observe(identity: RequestIdentity): Flow<MockApiUiState> {
        val dao = dao() ?: return flowOf(MockApiUiState())
        return combine(
            dao.observeConfig(identity.identityKey),
            dao.observeCustom(identity.identityKey)
        ) { config, customs ->
            MockApiUiState(
                enabled = config?.enabled ?: true,
                activeScenarioKey = config?.activeScenarioKey,
                slowDelayMs = config?.slowDelayMs ?: 1_000L,
                customScenarios = customs.map { it.toData() }
            )
        }
    }

    suspend fun getConfig(identity: RequestIdentity): MockApiConfigEntity? =
        dao()?.getConfig(identity.identityKey)

    suspend fun getCustom(id: Long): CustomScenarioData? =
        dao()?.getCustom(id)?.toData()

    suspend fun setApiEnabled(identity: RequestIdentity, enabled: Boolean) {
        val dao = dao() ?: return
        val existing = dao.getConfig(identity.identityKey)
        dao.upsertConfig(base(identity, existing).copy(enabled = enabled))
    }

    suspend fun activateScenario(identity: RequestIdentity, key: String) {
        val dao = dao() ?: return
        val existing = dao.getConfig(identity.identityKey)
        val nextKey = MockScenarioEngine.activateExclusive(existing?.activeScenarioKey, key)
        dao.upsertConfig(base(identity, existing).copy(enabled = true, activeScenarioKey = nextKey))
    }

    suspend fun setSlowDelay(identity: RequestIdentity, delayMs: Long) {
        val dao = dao() ?: return
        val existing = dao.getConfig(identity.identityKey)
        dao.upsertConfig(base(identity, existing).copy(slowDelayMs = delayMs.coerceAtLeast(0L)))
    }

    suspend fun saveSnapshot(
        identity: RequestIdentity,
        status: Int,
        headersJson: String,
        body: String
    ) {
        val dao = dao() ?: return
        val existing = dao.getConfig(identity.identityKey)
        dao.upsertConfig(
            base(identity, existing).copy(
                snapshotBody = body,
                snapshotStatus = if (status in 100..599) status else 200,
                snapshotHeadersJson = headersJson
            )
        )
    }

    suspend fun insertCustom(
        identity: RequestIdentity,
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
                identityKey = identity.identityKey,
                url = identity.url,
                method = identity.method,
                name = name,
                description = description,
                statusCode = statusCode,
                body = body,
                headersJson = headersToJson(headers),
                delayMs = delayMs
            )
        )
    }

    suspend fun updateCustom(data: CustomScenarioData, identity: RequestIdentity) {
        val dao = dao() ?: return
        dao.updateCustom(
            MockCustomScenarioEntity(
                id = data.id,
                identityKey = identity.identityKey,
                url = identity.url,
                method = identity.method,
                name = data.name,
                description = data.description,
                statusCode = data.statusCode,
                body = data.body,
                headersJson = headersToJson(data.headers),
                delayMs = data.delayMs
            )
        )
    }

    suspend fun deleteCustom(id: Long, identity: RequestIdentity) {
        val dao = dao() ?: return
        val config = dao.getConfig(identity.identityKey)
        if (config?.activeScenarioKey == customKey(id)) {
            dao.upsertConfig(config.copy(activeScenarioKey = null))
        }
        dao.deleteCustom(id)
    }

    private fun base(identity: RequestIdentity, existing: MockApiConfigEntity?): MockApiConfigEntity {
        return (existing ?: MockApiConfigEntity(
            identityKey = identity.identityKey,
            url = identity.url,
            method = identity.method,
            protocol = identity.protocol.name,
            gqlOperationType = identity.graphQlOperationType?.name,
            gqlOperationName = identity.graphQlOperationName
        )).copy(
            url = identity.url,
            method = identity.method,
            protocol = identity.protocol.name,
            gqlOperationType = identity.graphQlOperationType?.name,
            gqlOperationName = identity.graphQlOperationName
        )
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
