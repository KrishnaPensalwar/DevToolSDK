package io.github.krishnapensalwar.devkit.mock.identity

import org.json.JSONObject
import java.security.MessageDigest

internal object GraphQlParser {

    fun parse(
        url: String,
        method: String,
        requestBody: String?,
        contentType: String? = null,
        queryParams: Map<String, String> = emptyMap()
    ): RequestIdentity? {
        val payload = extractPayload(requestBody, contentType, queryParams) ?: return null
        val document = payload.query.trim()
        if (document.isEmpty()) return null

        val inferred = inferOperation(document)
        val operationType = inferred.type
        val jsonName = payload.operationName?.trim()?.takeIf { it.isNotEmpty() }
        val nameFromDocument = inferred.name
        val operationName = jsonName ?: nameFromDocument
        val normalized = normalizeDocument(document)
        val hash = sha256Prefix(normalized)
        val identityName = operationName ?: "anonymous-$hash"
        val methodUpper = method.uppercase()
        val endpointUrl = stripUrlQuery(url)
        val identityKey = buildString {
            append("gql|")
            append(methodUpper)
            append("|")
            append(endpointUrl)
            append("|")
            append(operationType.name)
            append("|")
            append(identityName)
        }

        return RequestIdentity(
            protocol = RequestProtocol.GRAPHQL,
            method = methodUpper,
            url = url,
            endpoint = RequestIdentity.pathOf(url),
            identityKey = identityKey,
            displayName = identityName,
            graphQlOperationType = operationType,
            graphQlOperationName = operationName,
            graphQlDocumentHash = hash
        )
    }

    fun looksLikeGraphQl(
        requestBody: String?,
        contentType: String?,
        url: String,
        queryParams: Map<String, String>
    ): Boolean = extractPayload(requestBody, contentType, queryParams) != null

    internal fun normalizeDocument(document: String): String {
        return stripComments(document).filterNot { it.isWhitespace() }
    }

    internal fun inferOperation(document: String): InferredOperation {
        val stripped = stripCommentsAndCollapse(document)
        val named = Regex(
            """\b(query|mutation|subscription)\s+([A-Za-z_][A-Za-z0-9_]*)""",
            RegexOption.IGNORE_CASE
        ).find(stripped)
        if (named != null) {
            return InferredOperation(
                type = typeFromKeyword(named.groupValues[1]),
                name = named.groupValues[2]
            )
        }
        val anonymousTyped = Regex(
            """\b(query|mutation|subscription)\s*[{(]""",
            RegexOption.IGNORE_CASE
        ).find(stripped)
        if (anonymousTyped != null) {
            return InferredOperation(typeFromKeyword(anonymousTyped.groupValues[1]), name = null)
        }
        if (stripped.startsWith("{")) {
            return InferredOperation(GraphQlOperationType.QUERY, name = null)
        }
        return InferredOperation(GraphQlOperationType.QUERY, name = null)
    }

    internal fun sha256Prefix(value: String, length: Int = 16): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { byte -> "%02x".format(byte) }.take(length)
    }

    internal data class InferredOperation(
        val type: GraphQlOperationType,
        val name: String?
    )

    private data class GraphQlPayload(
        val query: String,
        val operationName: String?
    )

    private fun extractPayload(
        requestBody: String?,
        contentType: String?,
        queryParams: Map<String, String>
    ): GraphQlPayload? {
        val type = contentType.orEmpty().lowercase()
        if (type.contains("application/graphql")) {
            val raw = requestBody?.trim().orEmpty()
            if (raw.isNotEmpty()) return GraphQlPayload(raw, null)
        }

        val fromQuery = queryParams["query"]?.trim().orEmpty()
        if (fromQuery.isNotEmpty()) {
            return GraphQlPayload(fromQuery, queryParams["operationName"])
        }

        val body = requestBody?.trim().orEmpty()
        if (body.startsWith("{")) {
            return try {
                val json = JSONObject(body)
                val query = json.optString("query").trim()
                if (query.isEmpty()) null
                else GraphQlPayload(
                    query = query,
                    operationName = json.optString("operationName").trim().takeIf { it.isNotEmpty() && it != "null" }
                )
            } catch (_: Exception) {
                null
            }
        }
        return null
    }

    private fun typeFromKeyword(keyword: String): GraphQlOperationType = when (keyword.lowercase()) {
        "mutation" -> GraphQlOperationType.MUTATION
        "subscription" -> GraphQlOperationType.SUBSCRIPTION
        else -> GraphQlOperationType.QUERY
    }

    private fun stripUrlQuery(url: String): String = url.substringBefore("?")

    private fun stripComments(document: String): String {
        val withoutBlock = document.replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), " ")
        return buildString {
            withoutBlock.lineSequence().forEach { line ->
                append(line.substringBefore("#"))
                append('\n')
            }
        }
    }

    private fun stripCommentsAndCollapse(document: String): String {
        return stripComments(document).replace(Regex("\\s+"), " ").trim()
    }
}
