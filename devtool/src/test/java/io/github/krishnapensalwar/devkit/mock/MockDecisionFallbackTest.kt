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
    fun restPrefersEditedCacheOverStaleSnapshot() {
        val edited = CapturedResponse(200, """{"edited":true}""", emptyMap())
        val snapshot = CapturedResponse(200, """{"original":true}""", emptyMap())
        val picked = capturedFromSources(rest, snapshot, identityCache = edited, restLegacy = null)
        assertEquals(edited.body, picked?.body)
    }

    @Test
    fun graphQlPrefersEditedIdentityCacheOverSnapshot() {
        val snapshot = CapturedResponse(200, """{"data":{"user":{"id":"1"}}}""", emptyMap())
        val edited = CapturedResponse(200, """{"data":{"user":{"id":"edited"}}}""", emptyMap())
        val picked = capturedFromSources(gql, snapshot, identityCache = edited, restLegacy = null)
        assertEquals(edited.body, picked?.body)
    }

    @Test
    fun cachedOverrideWinsOverSelectedScenario() {
        val decision = serveCachedOverride(useCachedBody = true, captured = snapshot)
        val serve = decision as MockDecision.Serve
        assertEquals("cache", serve.plan.scenarioKey)
        assertEquals(snapshot.body, serve.plan.body)
    }

    @Test
    fun scenarioModeDoesNotUseCachedOverrideHelper() {
        assertEquals(null, serveCachedOverride(useCachedBody = false, captured = snapshot))
    }

    @Test
    fun shouldNotSeedSnapshotWhenEditedBodyExists() {
        org.junit.Assert.assertFalse(shouldSeedSnapshot("""{"edited":true}"""))
        org.junit.Assert.assertTrue(shouldSeedSnapshot(null))
        org.junit.Assert.assertTrue(shouldSeedSnapshot("  "))
    }
}
