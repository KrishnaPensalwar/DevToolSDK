package io.github.krishnapensalwar.devkit.mock.identity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GraphQlParserTest {

    private val endpoint = "https://shop.test/graphql"

    @Test
    fun namedQueryUsesOperationNameNotUrlAlone() {
        val body = """{"query":"query GetProducts { products { id } }","operationName":"GetProducts","variables":{"limit":10}}"""
        val identity = RequestIdentity.parse(endpoint, "POST", body)
        assertTrue(identity.isGraphQl)
        assertEquals(GraphQlOperationType.QUERY, identity.graphQlOperationType)
        assertEquals("GetProducts", identity.graphQlOperationName)
        assertEquals("GetProducts", identity.displayName)
        assertTrue(identity.identityKey.contains("QUERY"))
        assertTrue(identity.identityKey.contains("GetProducts"))
    }

    @Test
    fun mutationIdentitySeparateFromQueryOnSameUrl() {
        val query = RequestIdentity.parse(
            endpoint, "POST",
            """{"query":"query GetUser { user { id } }","operationName":"GetUser"}"""
        )
        val mutation = RequestIdentity.parse(
            endpoint, "POST",
            """{"query":"mutation Login { login { token } }","operationName":"Login"}"""
        )
        assertEquals(query.url.substringBefore("?"), mutation.url.substringBefore("?"))
        assertNotEquals(query.identityKey, mutation.identityKey)
        assertEquals(GraphQlOperationType.MUTATION, mutation.graphQlOperationType)
        assertEquals("Login", mutation.displayName)
    }

    @Test
    fun variablesDoNotCreateSeparateApis() {
        val a = RequestIdentity.parse(
            endpoint, "POST",
            """{"query":"query GetProducts { products { id } }","operationName":"GetProducts","variables":{"limit":1}}"""
        )
        val b = RequestIdentity.parse(
            endpoint, "POST",
            """{"query":"query GetProducts { products { id } }","operationName":"GetProducts","variables":{"limit":99,"cursor":"abc"}}"""
        )
        assertEquals(a.identityKey, b.identityKey)
    }

    @Test
    fun unnamedOperationUsesDocumentHash() {
        val body = """{"query":"{ user { id name } }"}"""
        val identity = RequestIdentity.parse(endpoint, "POST", body)
        assertTrue(identity.isGraphQl)
        assertNull(identity.graphQlOperationName)
        assertTrue(identity.displayName.startsWith("anonymous-"))
        assertEquals(16, identity.graphQlDocumentHash?.length)
    }

    @Test
    fun unnamedSameDocumentSameHashDespiteWhitespace() {
        val compact = RequestIdentity.parse(endpoint, "POST", """{"query":"{user{id}}"}""")
        val spaced = RequestIdentity.parse(endpoint, "POST", """{"query":"{\n  user {\n    id\n  }\n}"}""")
        assertEquals(compact.identityKey, spaced.identityKey)
    }

    @Test
    fun operationNameFromDocumentWhenJsonNameMissing() {
        val identity = RequestIdentity.parse(
            endpoint, "POST",
            """{"query":"mutation CreateOrder { createOrder { id } }"}"""
        )
        assertEquals("CreateOrder", identity.graphQlOperationName)
        assertEquals(GraphQlOperationType.MUTATION, identity.graphQlOperationType)
    }

    @Test
    fun jsonOperationNameWinsOverDocumentName() {
        val identity = RequestIdentity.parse(
            endpoint, "POST",
            """{"query":"query Foo { x } query Bar { y }","operationName":"Bar"}"""
        )
        assertEquals("Bar", identity.graphQlOperationName)
    }

    @Test
    fun restJsonWithoutQueryFieldIsNotGraphQl() {
        val identity = RequestIdentity.parse(
            "https://shop.test/api/orders",
            "POST",
            """{"orderId":1}"""
        )
        assertFalse(identity.isGraphQl)
        assertTrue(identity.identityKey.startsWith("rest|"))
    }

    @Test
    fun graphqlNotIdentifiedByUrlAlone() {
        val identity = RequestIdentity.parse(
            "https://shop.test/graphql",
            "POST",
            """{"hello":"world"}"""
        )
        assertFalse(identity.isGraphQl)
    }

    @Test
    fun applicationGraphqlContentType() {
        val identity = RequestIdentity.parse(
            url = endpoint,
            method = "POST",
            requestBody = "query GetUser { user { id } }",
            contentType = "application/graphql"
        )
        assertTrue(identity.isGraphQl)
        assertEquals("GetUser", identity.graphQlOperationName)
    }

    @Test
    fun getQueryStringGraphQl() {
        val identity = RequestIdentity.parse(
            url = "$endpoint?query=query+GetUser+%7B+user+%7B+id+%7D+%7D&operationName=GetUser",
            method = "GET",
            requestBody = null,
            queryParams = mapOf(
                "query" to "query GetUser { user { id } }",
                "operationName" to "GetUser"
            )
        )
        assertTrue(identity.isGraphQl)
        assertEquals("GetUser", identity.displayName)
    }

    @Test
    fun restAndGraphqlCoexistOnDifferentKeys() {
        val rest = RequestIdentity.parse("https://shop.test/api/user", "GET", null)
        val gql = RequestIdentity.parse(
            endpoint, "POST",
            """{"query":"query GetUser { user { id } }","operationName":"GetUser"}"""
        )
        assertNotEquals(rest.identityKey, gql.identityKey)
        assertFalse(rest.isGraphQl)
        assertTrue(gql.isGraphQl)
    }

    @Test
    fun inferAnonymousTypedMutation() {
        val inferred = GraphQlParser.inferOperation("mutation { login { token } }")
        assertEquals(GraphQlOperationType.MUTATION, inferred.type)
        assertNull(inferred.name)
    }
}
