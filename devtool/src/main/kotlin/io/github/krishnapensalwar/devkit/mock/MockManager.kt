package io.github.krishnapensalwar.devkit.mock

import android.content.Context
import io.github.krishnapensalwar.devkit.MockResponse
import io.github.krishnapensalwar.devkit.internal.database.DevToolDatabase
import io.github.krishnapensalwar.devkit.mock.scenario.CapturedResponse
import io.github.krishnapensalwar.devkit.mock.scenario.MockFailureKind
import io.github.krishnapensalwar.devkit.mock.scenario.MockPlan
import io.github.krishnapensalwar.devkit.mock.scenario.MockScenarioEngine
import io.github.krishnapensalwar.devkit.mock.scenario.MockScenarioRepository
import io.github.krishnapensalwar.devkit.mock.scenario.parseCustomId
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.io.IOException

internal sealed class MockDecision {
    data class Serve(val plan: MockPlan) : MockDecision()
    object PassThrough : MockDecision()
    object Missing : MockDecision()
}

internal sealed class MockResolution {
    data class Http(val response: Response, val plan: MockPlan) : MockResolution()
    data class Failure(val exception: IOException, val plan: MockPlan) : MockResolution()
    object PassThrough : MockResolution()
    object Missing : MockResolution()
}

internal object MockManager {
    private lateinit var db: DevToolDatabase
    private var customResolver: ((HttpRequestBuilder) -> MockResponse?)? = null
    private var mockingEnabled: Boolean = false
    private val _mockingEnabledFlow = MutableStateFlow(false)
    val mockingEnabledFlow: StateFlow<Boolean> = _mockingEnabledFlow.asStateFlow()

    fun init(database: DevToolDatabase, context: Context) {
        db = database
        MockSafety.init(context)
        MockScenarioRepository.init(database)
    }

    fun getDbOrNull(): DevToolDatabase? {
        return if (::db.isInitialized) db else null
    }

    fun setMockingEnabled(enabled: Boolean) {
        mockingEnabled = enabled && MockSafety.debugBuild
        _mockingEnabledFlow.value = isMockingEnabled()
    }

    fun isMockingEnabled(): Boolean = MockSafety.allowMocking(mockingEnabled)

    fun setCustomResolver(resolver: (HttpRequestBuilder) -> MockResponse?) {
        customResolver = resolver
    }

    fun customResolver(): ((HttpRequestBuilder) -> MockResponse?)? = customResolver

    fun resolveOkHttp(request: Request): Response? {
        return when (val resolution = resolve(request, null)) {
            is MockResolution.Http -> resolution.response
            else -> null
        }
    }

    fun resolve(request: Request, requestBody: String? = null): MockResolution {
        val identity = identityFor(request, requestBody)
        return when (val decision = decide(identity)) {
            MockDecision.PassThrough -> MockResolution.PassThrough
            MockDecision.Missing -> MockResolution.Missing
            is MockDecision.Serve -> decision.plan.toResolution(request)
        }
    }

    fun identityFor(request: Request, requestBody: String? = null): io.github.krishnapensalwar.devkit.mock.identity.RequestIdentity {
        val params = buildMap {
            for (i in 0 until request.url.querySize) {
                put(request.url.queryParameterName(i), request.url.queryParameterValue(i).orEmpty())
            }
        }
        return io.github.krishnapensalwar.devkit.mock.identity.RequestIdentity.parse(
            url = request.url.toString(),
            method = request.method,
            requestBody = requestBody,
            contentType = request.header("Content-Type"),
            queryParams = params
        )
    }

    fun decide(identity: io.github.krishnapensalwar.devkit.mock.identity.RequestIdentity): MockDecision {
        if (!isMockingEnabled()) return MockDecision.PassThrough
        val database = getDbOrNull() ?: return MockDecision.Missing

        return runBlocking(Dispatchers.IO) {
            val config = MockScenarioRepository.getConfig(identity)
            if (config != null && !config.enabled) {
                return@runBlocking MockDecision.PassThrough
            }

            val captured = loadCaptured(database, identity, config)
            serveCachedOverride(config?.useCachedBody == true, captured)?.let {
                return@runBlocking it
            }

            val activeKey = config?.activeScenarioKey
            if (!activeKey.isNullOrBlank()) {
                val customId = parseCustomId(activeKey)
                val custom = customId?.let { MockScenarioRepository.getCustom(it) }
                val plan = MockScenarioEngine.resolve(
                    key = activeKey,
                    captured = captured,
                    slowDelayMs = config?.slowDelayMs ?: 1_000L,
                    custom = custom
                )
                if (plan != null) {
                    return@runBlocking MockDecision.Serve(plan)
                }
            }

            decisionAfterNoActiveScenario(identity, captured)?.let {
                return@runBlocking it
            }

            val mockEntity = database.mockDao().findMock(identity.url, identity.method)
            if (mockEntity != null && mockEntity.enabled) {
                val headers = MockScenarioRepository.jsonToHeaders(mockEntity.headers ?: "")
                return@runBlocking MockDecision.Serve(
                    MockPlan(
                        scenarioKey = "legacy",
                        scenarioName = "Saved mock",
                        statusCode = 200,
                        message = "Mocked OK",
                        body = mockEntity.responseBody,
                        headers = headers.ifEmpty { mapOf("Content-Type" to "application/json") },
                        delayMs = 0,
                        failure = MockFailureKind.NONE
                    )
                )
            }

            val cachedEntity = database.cachedResponseDao().getByIdentityKey(identity.identityKey)
            if (cachedEntity != null) {
                return@runBlocking MockDecision.Serve(
                    capturedToCachePlan(
                        CapturedResponse(
                            status = cachedEntity.status,
                            body = cachedEntity.body,
                            headers = MockScenarioRepository.jsonToHeaders(cachedEntity.headersJson)
                        )
                    )
                )
            }

            MockDecision.Missing
        }
    }

    private suspend fun loadCaptured(
        database: DevToolDatabase,
        identity: io.github.krishnapensalwar.devkit.mock.identity.RequestIdentity,
        config: io.github.krishnapensalwar.devkit.internal.database.MockApiConfigEntity?
    ): CapturedResponse? {
        val snapshot = config?.snapshotBody?.let { body ->
            CapturedResponse(
                status = config.snapshotStatus,
                body = body,
                headers = MockScenarioRepository.jsonToHeaders(config.snapshotHeadersJson.orEmpty())
            )
        }
        if (identity.isGraphQl) {
            val gqlCache = database.cachedResponseDao().getByIdentityKey(identity.identityKey)?.let {
                CapturedResponse(
                    status = it.status,
                    body = it.body,
                    headers = MockScenarioRepository.jsonToHeaders(it.headersJson)
                )
            }
            return capturedFromSources(identity, snapshot, gqlCache, restLegacy = null)
        }
        val restCache = database.cachedResponseDao().getByIdentityKey(identity.identityKey)?.let {
            CapturedResponse(
                status = it.status,
                body = it.body,
                headers = MockScenarioRepository.jsonToHeaders(it.headersJson)
            )
        } ?: database.cachedResponseDao().get(identity.url, identity.method)?.let {
            CapturedResponse(
                status = it.status,
                body = it.body,
                headers = MockScenarioRepository.jsonToHeaders(it.headersJson)
            )
        }
        val restLegacy = database.mockDao().findMock(identity.url, identity.method)?.let {
            CapturedResponse(
                status = 200,
                body = it.responseBody,
                headers = MockScenarioRepository.jsonToHeaders(it.headers ?: "")
            )
        }
        return capturedFromSources(identity, snapshot, restCache, restLegacy)
    }
}

internal fun MockPlan.toException(): IOException = when (failure) {
    MockFailureKind.TIMEOUT -> SocketTimeoutException("DevTool mock: timeout ($scenarioName)")
    MockFailureKind.NO_INTERNET -> UnknownHostException("DevTool mock: no internet ($scenarioName)")
    MockFailureKind.CONNECTION_FAILURE -> ConnectException("DevTool mock: connection failure ($scenarioName)")
    MockFailureKind.NONE -> IOException("DevTool mock failure ($scenarioName)")
}

private fun MockPlan.toResolution(request: Request): MockResolution {
    return if (failure != MockFailureKind.NONE) {
        MockResolution.Failure(toException(), this)
    } else {
        MockResolution.Http(toOkHttp(request), this)
    }
}

private fun MockPlan.toOkHttp(request: Request): Response {
    val mediaType = (headers["Content-Type"] ?: "application/json").toMediaTypeOrNull()
    val builder = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(statusCode.coerceIn(100, 599))
        .message(message)
        .body(body.toResponseBody(mediaType))
        .addHeader("X-Mock-Source", "Scenario")
        .addHeader("X-Mock-Scenario", scenarioName)
        .addHeader("X-Mock-Key", scenarioKey)

    headers.forEach { (key, value) ->
        if (!key.equals("X-Mock-Source", true) &&
            !key.equals("X-Mock-Scenario", true) &&
            !key.equals("X-Mock-Key", true)
        ) {
            builder.addHeader(key, value)
        }
    }
    return builder.build()
}

internal fun parseHeadersJson(headersJson: String?): Map<String, String> {
    if (headersJson.isNullOrBlank()) return emptyMap()
    return try {
        val jsonObject = JSONObject(headersJson)
        buildMap {
            jsonObject.keys().forEach { key -> put(key, jsonObject.getString(key)) }
        }
    } catch (_: Exception) {
        emptyMap()
    }
}
