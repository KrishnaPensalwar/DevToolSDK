package io.github.krishnapensalwar.devkit.mock.scenario

internal object MockScenarioEngine {

    fun resolve(
        key: String,
        captured: CapturedResponse?,
        slowDelayMs: Long,
        custom: CustomScenarioData?
    ): MockPlan? {
        parseCustomId(key)?.let {
            val data = custom ?: return null
            return planFromCustom(data)
        }
        val type = runCatching { MockScenarioType.valueOf(key) }.getOrNull() ?: return null
        return resolveType(type, captured, slowDelayMs, custom)
    }

    fun resolveType(
        type: MockScenarioType,
        captured: CapturedResponse?,
        slowDelayMs: Long,
        custom: CustomScenarioData? = null
    ): MockPlan {
        val builtIn = MockScenarioCatalog.find(type)
        val successBody = captured?.body ?: "{}"
        val successHeaders = captured?.headers ?: emptyMap()
        val delay = slowDelayMs.coerceAtLeast(0L)

        return when (type) {
            MockScenarioType.SUCCESS -> httpPlan(
                type = type,
                name = builtIn?.title ?: "Success",
                status = 200,
                body = successBody,
                headers = successHeaders
            )
            MockScenarioType.EMPTY_RESPONSE -> httpPlan(
                type = type,
                name = builtIn?.title ?: "Empty Response",
                status = 200,
                body = emptyResponseBody(successBody),
                headers = jsonHeaders()
            )
            MockScenarioType.EMPTY_BODY -> httpPlan(
                type = type,
                name = builtIn?.title ?: "Empty Body",
                status = 200,
                body = "",
                headers = jsonHeaders()
            )
            MockScenarioType.HTTP_400,
            MockScenarioType.HTTP_401,
            MockScenarioType.HTTP_403,
            MockScenarioType.HTTP_404,
            MockScenarioType.HTTP_409,
            MockScenarioType.HTTP_422,
            MockScenarioType.HTTP_429,
            MockScenarioType.HTTP_500,
            MockScenarioType.HTTP_502,
            MockScenarioType.HTTP_503,
            MockScenarioType.HTTP_504 -> {
                val code = builtIn?.statusCode ?: 500
                httpPlan(
                    type = type,
                    name = builtIn?.title ?: "HTTP $code",
                    status = code,
                    body = defaultErrorBody(code),
                    headers = jsonHeaders()
                )
            }
            MockScenarioType.TIMEOUT -> MockPlan(
                scenarioKey = type.name,
                scenarioName = builtIn?.title ?: "Timeout",
                statusCode = 0,
                message = "Timeout",
                body = "",
                headers = emptyMap(),
                delayMs = 0,
                failure = MockFailureKind.TIMEOUT
            )
            MockScenarioType.NO_INTERNET -> MockPlan(
                scenarioKey = type.name,
                scenarioName = builtIn?.title ?: "No Internet",
                statusCode = 0,
                message = "No Internet",
                body = "",
                headers = emptyMap(),
                delayMs = 0,
                failure = MockFailureKind.NO_INTERNET
            )
            MockScenarioType.CONNECTION_FAILURE -> MockPlan(
                scenarioKey = type.name,
                scenarioName = builtIn?.title ?: "Connection Failure",
                statusCode = 0,
                message = "Connection Failure",
                body = "",
                headers = emptyMap(),
                delayMs = 0,
                failure = MockFailureKind.CONNECTION_FAILURE
            )
            MockScenarioType.SLOW_RESPONSE -> httpPlan(
                type = type,
                name = builtIn?.title ?: "Slow Response",
                status = 200,
                body = successBody,
                headers = successHeaders,
                delayMs = delay
            )
            MockScenarioType.CUSTOM -> planFromCustom(
                custom ?: CustomScenarioData(
                    id = 0,
                    name = "Custom",
                    description = "",
                    statusCode = 200,
                    body = successBody,
                    headers = jsonHeaders(),
                    delayMs = 0
                )
            )
        }
    }

    fun emptyResponseBody(original: String?): String {
        val trimmed = original?.trim().orEmpty()
        return if (trimmed.startsWith("[")) "[]" else "{}"
    }

    fun defaultErrorBody(status: Int): String {
        val label = MockScenarioCatalog.statusMessage(status)
        return """{"error":"$label","status":$status,"mocked":true}"""
    }

    fun activateExclusive(currentKey: String?, selectedKey: String): String? {
        return if (currentKey == selectedKey) null else selectedKey
    }

    private fun planFromCustom(custom: CustomScenarioData): MockPlan {
        return MockPlan(
            scenarioKey = custom.key,
            scenarioName = custom.name.ifBlank { "Custom" },
            statusCode = custom.statusCode,
            message = MockScenarioCatalog.statusMessage(custom.statusCode),
            body = custom.body,
            headers = if (custom.headers.isEmpty()) jsonHeaders() else custom.headers,
            delayMs = custom.delayMs.coerceAtLeast(0L),
            failure = MockFailureKind.NONE
        )
    }

    private fun httpPlan(
        type: MockScenarioType,
        name: String,
        status: Int,
        body: String,
        headers: Map<String, String>,
        delayMs: Long = 0
    ): MockPlan = MockPlan(
        scenarioKey = type.name,
        scenarioName = name,
        statusCode = status,
        message = MockScenarioCatalog.statusMessage(status),
        body = body,
        headers = headers,
        delayMs = delayMs,
        failure = MockFailureKind.NONE
    )

    private fun jsonHeaders(): Map<String, String> = mapOf("Content-Type" to "application/json")
}
