package io.github.krishnapensalwar.devkit.network.model

import io.github.krishnapensalwar.devkit.mock.identity.RequestIdentity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkCallSearchTest {

    private fun call(
        url: String = "https://shop.test/graphql",
        host: String = "shop.test",
        endpoint: String = "/graphql",
        requestBody: String? = """{"query":"mutation Landing { landing { ok } }","operationName":"Landing"}"""
    ) = NetworkCall(
        url = url,
        endpoint = endpoint,
        host = host,
        method = "POST",
        requestHeaders = emptyMap(),
        requestBody = requestBody,
        requestSize = 0,
        responseHeaders = emptyMap(),
        responseBody = "{}",
        responseSize = 2,
        statusCode = 200,
        statusMessage = "OK",
        duration = 1,
        timestamp = 0,
        success = true,
        exception = null,
        protocol = "HTTP/1.1"
    )

    @Test
    fun landingMatchesGraphQlOperationNameNotOnlyHost() {
        val networkCall = call()
        val identity = RequestIdentity.parse(networkCall.url, networkCall.method, networkCall.requestBody)
        assertTrue(networkCall.matchesQuery("landing", identity))
        assertTrue(networkCall.matchesQuery("LANDING", identity))
        assertFalse(networkCall.matchesQuery("orders", identity))
    }

    @Test
    fun hostSearchStillWorks() {
        val networkCall = call()
        val identity = RequestIdentity.parse(networkCall.url, networkCall.method, networkCall.requestBody)
        assertTrue(networkCall.matchesQuery("shop.test", identity))
    }
}
