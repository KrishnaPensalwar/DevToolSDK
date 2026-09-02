package io.github.krishnapensalwar.devkit.mock

import io.github.krishnapensalwar.devkit.mock.identity.RequestIdentity
import io.github.krishnapensalwar.devkit.mock.scenario.CapturedResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockDecisionFallbackTest {

    private val gql = RequestIdentity.parse(
        url = "https://shop.test/graphql",
        method = "POST",
        requestBody = """{"query":"query GetUser { user { id } }","operationName":"GetUser"}"""
    )

    private val rest = RequestIdentity.parse(
        url = "https://shop.test/api/user",
        method = "GET",
        requestBody = null
    )

    private val snapshot = CapturedResponse(
        status = 200,
        body = """{"data":{"user":{"id":"1"}}}""",
        headers = mapOf("Content-Type" to "application/json")
    )

    @Test
    fun graphQlWithSnapshotServesCacheWhenGlobalMockOn() {
        val decision = decisionAfterNoActiveScenario(gql, snapshot)
        val serve = decision as MockDecision.Serve
        assertEquals("cache", serve.plan.scenarioKey)
        assertEquals(snapshot.body, serve.plan.body)
        assertEquals(200, serve.plan.statusCode)
    }

    @Test
    fun graphQlWithoutSnapshotPassesThrough() {
        val decision = decisionAfterNoActiveScenario(gql, captured = null)
        assertEquals(MockDecision.PassThrough, decision)
    }

    @Test
    fun restWithoutSnapshotLeavesRestLookups() {
        assertEquals(null, decisionAfterNoActiveScenario(rest, captured = null))
    }

    @Test
    fun restIdentityIsNotGraphQl() {
        assertTrue(!rest.isGraphQl)
        assertTrue(gql.isGraphQl)
    }
}
