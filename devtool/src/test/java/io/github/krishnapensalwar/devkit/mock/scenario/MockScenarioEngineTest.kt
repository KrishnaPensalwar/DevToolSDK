package io.github.krishnapensalwar.devkit.mock.scenario

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MockScenarioEngineTest {

    private val captured = CapturedResponse(
        status = 200,
        body = """{"items":[1,2]}""",
        headers = mapOf("Content-Type" to "application/json")
    )

    @Test
    fun successUsesCapturedBodyAnd200() {
        val plan = MockScenarioEngine.resolveType(MockScenarioType.SUCCESS, captured, 0)
        assertEquals(200, plan.statusCode)
        assertEquals(captured.body, plan.body)
        assertEquals(MockFailureKind.NONE, plan.failure)
        assertEquals(0L, plan.delayMs)
        assertEquals("SUCCESS", plan.scenarioKey)
    }

    @Test
    fun emptyResponseUsesEmptyObjectWhenBodyIsObject() {
        val plan = MockScenarioEngine.resolveType(MockScenarioType.EMPTY_RESPONSE, captured, 0)
        assertEquals(200, plan.statusCode)
        assertEquals("{}", plan.body)
    }

    @Test
    fun emptyResponseUsesEmptyArrayWhenBodyIsArray() {
        val listCaptured = captured.copy(body = """[{"id":1}]""")
        val plan = MockScenarioEngine.resolveType(MockScenarioType.EMPTY_RESPONSE, listCaptured, 0)
        assertEquals("[]", plan.body)
    }

    @Test
    fun emptyBodyIsBlank() {
        val plan = MockScenarioEngine.resolveType(MockScenarioType.EMPTY_BODY, captured, 0)
        assertEquals("", plan.body)
        assertEquals(200, plan.statusCode)
    }

    @Test
    fun clientErrorsReturnMatchingStatusAndJson() {
        val plan = MockScenarioEngine.resolveType(MockScenarioType.HTTP_401, captured, 0)
        assertEquals(401, plan.statusCode)
        assertTrue(plan.body.contains("401"))
        assertTrue(plan.body.contains("Unauthorized"))
        assertEquals(MockFailureKind.NONE, plan.failure)
    }

    @Test
    fun serverErrorsReturnMatchingStatus() {
        val plan = MockScenarioEngine.resolveType(MockScenarioType.HTTP_500, captured, 0)
        assertEquals(500, plan.statusCode)
        assertTrue(plan.body.contains("Internal Server Error"))
    }

    @Test
    fun timeoutIsNetworkFailureNotHttp() {
        val plan = MockScenarioEngine.resolveType(MockScenarioType.TIMEOUT, captured, 0)
        assertEquals(MockFailureKind.TIMEOUT, plan.failure)
        assertEquals(0, plan.statusCode)
    }

    @Test
    fun noInternetIsUnknownHostFailure() {
        val plan = MockScenarioEngine.resolveType(MockScenarioType.NO_INTERNET, captured, 0)
        assertEquals(MockFailureKind.NO_INTERNET, plan.failure)
    }

    @Test
    fun connectionFailureKind() {
        val plan = MockScenarioEngine.resolveType(MockScenarioType.CONNECTION_FAILURE, captured, 0)
        assertEquals(MockFailureKind.CONNECTION_FAILURE, plan.failure)
    }

    @Test
    fun slowResponseKeepsBodyAndAppliesDelay() {
        val plan = MockScenarioEngine.resolveType(MockScenarioType.SLOW_RESPONSE, captured, 2_000)
        assertEquals(200, plan.statusCode)
        assertEquals(captured.body, plan.body)
        assertEquals(2_000L, plan.delayMs)
        assertEquals(MockFailureKind.NONE, plan.failure)
    }

    @Test
    fun customScenarioUsesProvidedFields() {
        val custom = CustomScenarioData(
            id = 7,
            name = "Payment Pending",
            description = "async",
            statusCode = 202,
            body = """{"status":"pending"}""",
            headers = mapOf("Retry-After" to "5"),
            delayMs = 1_500
        )
        val plan = MockScenarioEngine.resolve("custom:7", captured, 0, custom)
        assertNotNull(plan)
        assertEquals("Payment Pending", plan!!.scenarioName)
        assertEquals(202, plan.statusCode)
        assertEquals("""{"status":"pending"}""", plan.body)
        assertEquals(1_500L, plan.delayMs)
        assertEquals("custom:7", plan.scenarioKey)
        assertEquals("5", plan.headers["Retry-After"])
    }

    @Test
    fun activateExclusiveTogglesOffWhenSameKeySelected() {
        assertNull(MockScenarioEngine.activateExclusive("HTTP_500", "HTTP_500"))
        assertEquals("HTTP_500", MockScenarioEngine.activateExclusive("HTTP_401", "HTTP_500"))
        assertEquals("TIMEOUT", MockScenarioEngine.activateExclusive(null, "TIMEOUT"))
    }

    @Test
    fun resolveUnknownKeyReturnsNull() {
        assertNull(MockScenarioEngine.resolve("not-a-scenario", captured, 0, null))
    }

    @Test
    fun emptyResponseHelper() {
        assertEquals("[]", MockScenarioEngine.emptyResponseBody("  [1] "))
        assertEquals("{}", MockScenarioEngine.emptyResponseBody("""{"a":1}"""))
        assertEquals("{}", MockScenarioEngine.emptyResponseBody(null))
    }
}
