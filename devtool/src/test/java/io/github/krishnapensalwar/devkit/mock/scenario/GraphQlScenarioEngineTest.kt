package io.github.krishnapensalwar.devkit.mock.scenario

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GraphQlScenarioEngineTest {

    private val captured = CapturedResponse(
        status = 200,
        body = """{"data":{"user":{"id":"1"},"orders":[{"id":"o1"}]}}"""
    )

    @Test
    fun successKeepsCapturedGraphQlBody() {
        val plan = MockScenarioEngine.resolveType(MockScenarioType.SUCCESS, captured, 0)
        assertEquals(200, plan.statusCode)
        assertEquals(captured.body, plan.body)
    }

    @Test
    fun emptyDataIsEmptyDataObject() {
        val plan = MockScenarioEngine.resolveType(MockScenarioType.EMPTY_DATA, captured, 0)
        assertEquals(200, plan.statusCode)
        assertEquals("""{"data":{}}""", plan.body)
    }

    @Test
    fun graphQlErrorIsHttp200WithErrorsArray() {
        val plan = MockScenarioEngine.resolveType(MockScenarioType.GRAPHQL_ERROR, captured, 0)
        assertEquals(200, plan.statusCode)
        assertTrue(plan.body.contains("\"data\":null"))
        assertTrue(plan.body.contains("errors"))
        assertTrue(plan.body.contains("User not found"))
    }

    @Test
    fun partialDataKeepsHttp200AndAddsErrors() {
        val plan = MockScenarioEngine.resolveType(MockScenarioType.GRAPHQL_PARTIAL, captured, 0)
        assertEquals(200, plan.statusCode)
        assertTrue(plan.body.contains("errors"))
        assertTrue(plan.body.contains("\"data\""))
    }

    @Test
    fun http500StillWorksForGraphQl() {
        val plan = MockScenarioEngine.resolveType(MockScenarioType.HTTP_500, captured, 0)
        assertEquals(500, plan.statusCode)
        assertTrue(plan.body.contains("500"))
    }

    @Test
    fun timeoutStillNetworkFailure() {
        val plan = MockScenarioEngine.resolveType(MockScenarioType.TIMEOUT, captured, 0)
        assertEquals(MockFailureKind.TIMEOUT, plan.failure)
    }

    @Test
    fun graphQlCatalogIncludesGraphQlSpecificScenarios() {
        val keys = MockScenarioCatalog.graphQlBuiltIns.map { it.key }.toSet()
        assertTrue(keys.contains("EMPTY_DATA"))
        assertTrue(keys.contains("GRAPHQL_ERROR"))
        assertTrue(keys.contains("GRAPHQL_PARTIAL"))
        assertTrue(keys.contains("SUCCESS"))
        assertTrue(keys.contains("HTTP_500"))
        assertTrue(keys.contains("TIMEOUT"))
        assertTrue(keys.contains("SLOW_RESPONSE"))
        assertFalseRestOnly(keys)
    }

    private fun assertFalseRestOnly(keys: Set<String>) {
        org.junit.Assert.assertFalse(keys.contains("EMPTY_RESPONSE"))
        org.junit.Assert.assertFalse(keys.contains("EMPTY_BODY"))
    }
}
