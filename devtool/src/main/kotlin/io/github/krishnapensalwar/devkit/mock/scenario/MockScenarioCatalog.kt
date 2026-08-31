package io.github.krishnapensalwar.devkit.mock.scenario

internal object MockScenarioCatalog {

    val slowDelayPresetsMs: List<Long> = listOf(500, 1_000, 2_000, 3_000, 5_000)

    val builtIns: List<BuiltInScenario> = listOf(
        BuiltInScenario(MockScenarioType.SUCCESS, MockScenarioGroup.SUCCESS, "Success", "200 OK with captured body", 200),
        BuiltInScenario(MockScenarioType.EMPTY_RESPONSE, MockScenarioGroup.SUCCESS, "Empty Response", "200 with empty object or list", 200),
        BuiltInScenario(MockScenarioType.EMPTY_BODY, MockScenarioGroup.SUCCESS, "Empty Body", "200 with blank body", 200),

        BuiltInScenario(MockScenarioType.HTTP_400, MockScenarioGroup.CLIENT_ERROR, "400 Bad Request", "Client error", 400),
        BuiltInScenario(MockScenarioType.HTTP_401, MockScenarioGroup.CLIENT_ERROR, "401 Unauthorized", "Client error", 401),
        BuiltInScenario(MockScenarioType.HTTP_403, MockScenarioGroup.CLIENT_ERROR, "403 Forbidden", "Client error", 403),
        BuiltInScenario(MockScenarioType.HTTP_404, MockScenarioGroup.CLIENT_ERROR, "404 Not Found", "Client error", 404),
        BuiltInScenario(MockScenarioType.HTTP_409, MockScenarioGroup.CLIENT_ERROR, "409 Conflict", "Client error", 409),
        BuiltInScenario(MockScenarioType.HTTP_422, MockScenarioGroup.CLIENT_ERROR, "422 Unprocessable Entity", "Client error", 422),
        BuiltInScenario(MockScenarioType.HTTP_429, MockScenarioGroup.CLIENT_ERROR, "429 Too Many Requests", "Client error", 429),

        BuiltInScenario(MockScenarioType.HTTP_500, MockScenarioGroup.SERVER_ERROR, "500 Internal Server Error", "Server error", 500),
        BuiltInScenario(MockScenarioType.HTTP_502, MockScenarioGroup.SERVER_ERROR, "502 Bad Gateway", "Server error", 502),
        BuiltInScenario(MockScenarioType.HTTP_503, MockScenarioGroup.SERVER_ERROR, "503 Service Unavailable", "Server error", 503),
        BuiltInScenario(MockScenarioType.HTTP_504, MockScenarioGroup.SERVER_ERROR, "504 Gateway Timeout", "Server error", 504),

        BuiltInScenario(MockScenarioType.TIMEOUT, MockScenarioGroup.NETWORK, "Timeout", "Throws SocketTimeoutException", null),
        BuiltInScenario(MockScenarioType.NO_INTERNET, MockScenarioGroup.NETWORK, "No Internet", "Throws UnknownHostException", null),
        BuiltInScenario(MockScenarioType.CONNECTION_FAILURE, MockScenarioGroup.NETWORK, "Connection Failure", "Throws ConnectException", null),

        BuiltInScenario(MockScenarioType.SLOW_RESPONSE, MockScenarioGroup.PERFORMANCE, "Slow Response", "Success body after configurable delay", 200)
    )

    val grouped: Map<MockScenarioGroup, List<BuiltInScenario>> =
        builtIns.groupBy { it.group }

    fun find(type: MockScenarioType): BuiltInScenario? = builtIns.find { it.type == type }

    fun findByKey(key: String): BuiltInScenario? = builtIns.find { it.key == key }

    fun groupLabel(group: MockScenarioGroup): String = when (group) {
        MockScenarioGroup.SUCCESS -> "SUCCESS"
        MockScenarioGroup.CLIENT_ERROR -> "4XX CLIENT ERRORS"
        MockScenarioGroup.SERVER_ERROR -> "5XX SERVER ERRORS"
        MockScenarioGroup.NETWORK -> "NETWORK"
        MockScenarioGroup.PERFORMANCE -> "PERFORMANCE"
        MockScenarioGroup.CUSTOM -> "CUSTOM"
    }

    fun statusMessage(code: Int): String = when (code) {
        200 -> "OK"
        202 -> "Accepted"
        204 -> "No Content"
        400 -> "Bad Request"
        401 -> "Unauthorized"
        403 -> "Forbidden"
        404 -> "Not Found"
        409 -> "Conflict"
        422 -> "Unprocessable Entity"
        429 -> "Too Many Requests"
        500 -> "Internal Server Error"
        502 -> "Bad Gateway"
        503 -> "Service Unavailable"
        504 -> "Gateway Timeout"
        else -> "Mocked"
    }
}
