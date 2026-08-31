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
    }

    fun isMockingEnabled(): Boolean = MockSafety.allowMocking(mockingEnabled)

    fun setCustomResolver(resolver: (HttpRequestBuilder) -> MockResponse?) {
        customResolver = resolver
    }

    fun customResolver(): ((HttpRequestBuilder) -> MockResponse?)? = customResolver

    fun resolveOkHttp(request: Request): Response? {
        return when (val resolution = resolve(request)) {
            is MockResolution.Http -> resolution.response
            else -> null
        }
    }

    fun resolve(request: Request): MockResolution {
        return when (val decision = decide(request.url.toString(), request.method)) {
            MockDecision.PassThrough -> MockResolution.PassThrough
            MockDecision.Missing -> MockResolution.Missing
            is MockDecision.Serve -> decision.plan.toResolution(request)
        }
    }

    fun decide(url: String, method: String): MockDecision {
        if (!isMockingEnabled()) return MockDecision.PassThrough
        val database = getDbOrNull() ?: return MockDecision.Missing

        return runBlocking(Dispatchers.IO) {
            val config = MockScenarioRepository.getConfig(url, method)
            if (config != null && !config.enabled) {
                return@runBlocking MockDecision.PassThrough
            }

            val captured = loadCaptured(database, url, method)
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

            val mockEntity = database.mockDao().findMock(url, method)
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

            val cachedEntity = database.cachedResponseDao().get(url, method)
            if (cachedEntity != null) {
                val headers = MockScenarioRepository.jsonToHeaders(cachedEntity.headersJson)
                return@runBlocking MockDecision.Serve(
                    MockPlan(
                        scenarioKey = "cache",
                        scenarioName = "Cached response",
                        statusCode = cachedEntity.status,
                        message = "Cached Response",
                        body = cachedEntity.body,
                        headers = headers.ifEmpty { mapOf("Content-Type" to "application/json") },
                        delayMs = 0,
                        failure = MockFailureKind.NONE
                    )
                )
            }

            MockDecision.Missing
        }
    }

    private suspend fun loadCaptured(
        database: DevToolDatabase,
        url: String,
        method: String
    ): CapturedResponse? {
        database.cachedResponseDao().get(url, method)?.let {
            return CapturedResponse(
                status = it.status,
                body = it.body,
                headers = MockScenarioRepository.jsonToHeaders(it.headersJson)
            )
        }
        database.mockDao().findMock(url, method)?.let {
            return CapturedResponse(
                status = 200,
                body = it.responseBody,
                headers = MockScenarioRepository.jsonToHeaders(it.headers ?: "")
            )
        }
        return null
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
