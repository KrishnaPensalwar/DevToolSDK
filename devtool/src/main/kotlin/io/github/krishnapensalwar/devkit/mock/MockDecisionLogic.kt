package io.github.krishnapensalwar.devkit.mock

import io.github.krishnapensalwar.devkit.mock.identity.RequestIdentity
import io.github.krishnapensalwar.devkit.mock.scenario.CapturedResponse
import io.github.krishnapensalwar.devkit.mock.scenario.MockFailureKind
import io.github.krishnapensalwar.devkit.mock.scenario.MockPlan

internal fun capturedToCachePlan(captured: CapturedResponse): MockPlan = MockPlan(
    scenarioKey = "cache",
    scenarioName = "Cached response",
    statusCode = captured.status.coerceIn(100, 599),
    message = "Cached Response",
    body = captured.body,
    headers = captured.headers.ifEmpty { mapOf("Content-Type" to "application/json") },
    delayMs = 0,
    failure = MockFailureKind.NONE
)

internal fun decisionAfterNoActiveScenario(
    identity: RequestIdentity,
    captured: CapturedResponse?
): MockDecision? {
    if (!identity.isGraphQl) return null
    if (captured != null) return MockDecision.Serve(capturedToCachePlan(captured))
    return MockDecision.PassThrough
}

internal fun capturedFromSources(
    identity: RequestIdentity,
    snapshot: CapturedResponse?,
    identityCache: CapturedResponse?,
    restLegacy: CapturedResponse?
): CapturedResponse? {
    if (identity.isGraphQl) return identityCache ?: snapshot
    return identityCache ?: snapshot ?: restLegacy
}

internal fun shouldSeedSnapshot(existingBody: String?): Boolean = existingBody.isNullOrBlank()

internal fun serveCachedOverride(
    useCachedBody: Boolean,
    captured: CapturedResponse?
): MockDecision? {
    if (!useCachedBody) return null
    if (captured != null) return MockDecision.Serve(capturedToCachePlan(captured))
    return null
}
