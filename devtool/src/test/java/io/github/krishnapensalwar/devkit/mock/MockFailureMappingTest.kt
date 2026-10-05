package io.github.krishnapensalwar.devkit.mock

import io.github.krishnapensalwar.devkit.mock.scenario.MockFailureKind
import io.github.krishnapensalwar.devkit.mock.scenario.MockPlan
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class MockFailureMappingTest {

    private fun plan(kind: MockFailureKind) = MockPlan(
        scenarioKey = kind.name,
        scenarioName = kind.name,
        statusCode = 0,
        message = kind.name,
        body = "",
        headers = emptyMap(),
        delayMs = 0,
        failure = kind
    )

    @Test
    fun timeoutMapsToSocketTimeoutException() {
        assertTrue(plan(MockFailureKind.TIMEOUT).toException() is SocketTimeoutException)
    }

    @Test
    fun noInternetMapsToUnknownHostException() {
        assertTrue(plan(MockFailureKind.NO_INTERNET).toException() is UnknownHostException)
    }

    @Test
    fun connectionFailureMapsToConnectException() {
        assertTrue(plan(MockFailureKind.CONNECTION_FAILURE).toException() is ConnectException)
    }
}
