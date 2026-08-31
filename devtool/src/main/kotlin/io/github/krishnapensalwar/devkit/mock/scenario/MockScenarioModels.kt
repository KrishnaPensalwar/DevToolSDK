package io.github.krishnapensalwar.devkit.mock.scenario

internal enum class MockScenarioGroup {
    SUCCESS,
    CLIENT_ERROR,
    SERVER_ERROR,
    NETWORK,
    PERFORMANCE,
    CUSTOM
}

internal enum class MockScenarioType {
    SUCCESS,
    EMPTY_RESPONSE,
    EMPTY_BODY,
    HTTP_400,
    HTTP_401,
    HTTP_403,
    HTTP_404,
    HTTP_409,
    HTTP_422,
    HTTP_429,
    HTTP_500,
    HTTP_502,
    HTTP_503,
    HTTP_504,
    TIMEOUT,
    NO_INTERNET,
    CONNECTION_FAILURE,
    SLOW_RESPONSE,
    CUSTOM
}

internal enum class MockFailureKind {
    NONE,
    TIMEOUT,
    NO_INTERNET,
    CONNECTION_FAILURE
}

internal data class BuiltInScenario(
    val type: MockScenarioType,
    val group: MockScenarioGroup,
    val title: String,
    val subtitle: String,
    val statusCode: Int?
) {
    val key: String get() = type.name
}

internal data class CapturedResponse(
    val status: Int,
    val body: String,
    val headers: Map<String, String> = emptyMap()
)

internal data class CustomScenarioData(
    val id: Long,
    val name: String,
    val description: String,
    val statusCode: Int,
    val body: String,
    val headers: Map<String, String>,
    val delayMs: Long
) {
    val key: String get() = customKey(id)
}

internal data class MockPlan(
    val scenarioKey: String,
    val scenarioName: String,
    val statusCode: Int,
    val message: String,
    val body: String,
    val headers: Map<String, String>,
    val delayMs: Long,
    val failure: MockFailureKind
)

internal fun customKey(id: Long): String = "custom:$id"

internal fun parseCustomId(key: String): Long? {
    if (!key.startsWith("custom:")) return null
    return key.removePrefix("custom:").toLongOrNull()
}
