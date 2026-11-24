
package io.github.krishnapensalwar.devkit.mock.identity

import java.security.MessageDigest

/**
 * Identifies GraphQL requests so the mocking engine can match them
 * with configured mock responses.
 *
 * This class identifies requests only. It does not intercept network calls.
 */
internal object GraphQlParser {

    /**
     * STEP 1: Extract the GraphQL query from the request.
     *
     * Supports:
     * - JSON body containing "query"
     * - application/graphql request body
     * - URL query parameters containing "query"
     */
    fun parse(
        url: String,
        method: String,
        requestBody: String?,
        contentType: String? = null,
        queryParams: Map<String, String> = emptyMap()
    ): RequestIdentity? {

        // STEP 2: Extract the GraphQL document and optional operation name.
        val payload = extractPayload(
            requestBody = requestBody,
            contentType = contentType,
            queryParams = queryParams
        ) ?: return null

        val document = payload.query.trim()
        if (document.isEmpty()) return null

        // STEP 3: Identify the operation type and name.
        // Example: query GetUser { ... } -> QUERY, GetUser
        val operation = inferOperation(document)

        // Prefer the operationName supplied in the JSON payload.
        // If it is missing, use the name inferred from the document.
        val operationName = payload.operationName
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: operation.name

        // STEP 4: Normalize the document and calculate a short hash.
        // The hash is useful for identifying anonymous operations.
        val normalizedDocument = normalizeDocument(document)
        val documentHash = sha256Prefix(normalizedDocument)

        // STEP 5: Create a display name.
        // Named operation -> GetUser
        // Anonymous operation -> anonymous-<hash>
        val identityName = operationName ?: "anonymous-$documentHash"

        // STEP 6: Build a stable key for mock lookup.
        // Ignore URL query parameters when identifying the endpoint.
        val normalizedMethod = method.uppercase()
        val endpointUrl = url.substringBefore("?")

        val identityKey = buildString {
            append("gql|")
            append(normalizedMethod)
            append("|")
            append(endpointUrl)
            append("|")
            append(operation.type.name)
            append("|")
            append(identityName)
        }

        // STEP 7: Return the identity used by the mocking engine.
        return RequestIdentity(
            protocol = RequestProtocol.GRAPHQL,
            method = normalizedMethod,
            url = url,
            endpoint = RequestIdentity.pathOf(url),
            identityKey = identityKey,
            displayName = identityName,
            graphQlOperationType = operation.type,
            graphQlOperationName = operationName,
            graphQlDocumentHash = documentHash
        )
    }

    /**
     * STEP 8: Extract the GraphQL payload from supported request formats.
     */
    private fun extractPayload(
        requestBody: String?,
        contentType: String?,
        queryParams: Map<String, String>
    ): GraphQlPayload? {

        val type = contentType.orEmpty().lowercase()

        // Format A: The body itself contains a GraphQL document.
        if (type.contains("application/graphql")) {
            val query = requestBody.orEmpty().trim()
            if (query.isNotEmpty()) {
                return GraphQlPayload(query, operationName = null)
            }
        }

        // Format B: The query is provided in URL query parameters.
        val queryFromUrl = queryParams["query"].orEmpty().trim()

        if (queryFromUrl.isNotEmpty()) {
            return GraphQlPayload(
                query = queryFromUrl,
                operationName = queryParams["operationName"]
            )
        }

        // Format C: The body is JSON containing "query" and optionally
        // "operationName".
        val body = requestBody.orEmpty().trim()

        if (body.startsWith("{")) {
            val query = jsonStringField(body, "query")
                ?.trim()
                .orEmpty()

            if (query.isEmpty()) return null

            val operationName = jsonStringField(body, "operationName")
                ?.trim()
                ?.takeIf { it.isNotEmpty() && it != "null" }

            return GraphQlPayload(query, operationName)
        }

        // No supported GraphQL payload was found.
        return null
    }

    /**
     * STEP 9: Infer the operation type and name from the document.
     */
    internal fun inferOperation(document: String): InferredOperation {

        // Remove comments and simplify whitespace before checking keywords.
        val text = stripComments(document)
            .replace(Regex("\\s+"), " ")
            .trim()

        // Named operations:
        // query GetUser { ... }
        // mutation UpdateUser { ... }
        // subscription ListenForMessages { ... }
        val namedOperation = Regex(
            """\b(query|mutation|subscription)\s+([A-Za-z_][A-Za-z0-9_]*)"""
        ).find(text)

        if (namedOperation != null) {
            return InferredOperation(
                type = typeFromKeyword(namedOperation.groupValues[1]),
                name = namedOperation.groupValues[2]
            )
        }

        // Explicit but anonymous operations:
        // query { ... }
        // mutation { ... }
        val anonymousOperation = Regex(
            """\b(query|mutation|subscription)\s*(?=\{| \()"""
                .replace("| ", "|")
        ).find(text)

        if (anonymousOperation != null) {
            return InferredOperation(
                type = typeFromKeyword(anonymousOperation.groupValues[1]),
                name = null
            )
        }

        // GraphQL shorthand syntax: { user { id } }
        // Shorthand operations are queries.
        return InferredOperation(
            type = GraphQlOperationType.QUERY,
            name = null
        )
    }

    /**
     * STEP 10: Normalize the document for hashing.
     *
     * NOTE: This lightweight implementation removes all whitespace.
     * It is not a complete GraphQL canonicalizer.
     */
    internal fun normalizeDocument(document: String): String {
        return stripComments(document)
            .filterNot { it.isWhitespace() }
    }

    /**
     * STEP 11: Generate a short SHA-256 fingerprint.
     */
    internal fun sha256Prefix(
        value: String,
        length: Int = 16
    ): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))

        return digest.joinToString("") { byte ->
            "%02x".format(byte)
        }.take(length)
    }

    /**
     * Reads a JSON string field without depending on Android's JSONObject.
     *
     * This is a lightweight implementation, not a complete JSON parser.
     */
    internal fun jsonStringField(json: String, key: String): String? {
        val pattern = Regex(
            "\"${Regex.escape(key)}\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
        )

        val match = pattern.find(json) ?: return null
        return unescapeJsonString(match.groupValues[1])
    }

    /**
     * Converts escaped JSON characters into their actual characters.
     * Example: \\n becomes a newline and \\" becomes a quote.
     */
    private fun unescapeJsonString(raw: String): String = buildString {
        var index = 0

        while (index < raw.length) {
            val char = raw[index]

            if (char == '\\' && index + 1 < raw.length) {
                val next = raw[index + 1]

                when (next) {
                    'n' -> {
                        append('\n')
                        index += 2
                    }

                    'r' -> {
                        append('\r')
                        index += 2
                    }

                    't' -> {
                        append('\t')
                        index += 2
                    }

                    '"', '\\', '/' -> {
                        append(next)
                        index += 2
                    }

                    'u' -> {
                        // Decode a Unicode escape such as \u0041.
                        if (index + 5 < raw.length) {
                            val hex = raw.substring(index + 2, index + 6)
                            append(hex.toIntOrNull(16)?.toChar() ?: '?')
                            index += 6
                        } else {
                            append(next)
                            index += 2
                        }
                    }

                    else -> {
                        append(next)
                        index += 2
                    }
                }
            } else {
                append(char)
                index++
            }
        }
    }

    // Helper: map the GraphQL keyword to the SDK's operation type.
    private fun typeFromKeyword(keyword: String): GraphQlOperationType {
        return when (keyword.lowercase()) {
            "mutation" -> GraphQlOperationType.MUTATION
            "subscription" -> GraphQlOperationType.SUBSCRIPTION
            else -> GraphQlOperationType.QUERY
        }
    }

    // Helper: remove GraphQL comments before inspecting the document.
    private fun stripComments(document: String): String {
        val withoutBlockComments = document.replace(
            Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL),
            " "
        )

        return withoutBlockComments
            .lineSequence()
            .joinToString("\n") { line ->
                line.substringBefore("#")
            }
    }

    // Temporary data extracted from the incoming request.
    private data class GraphQlPayload(
        val query: String,
        val operationName: String?
    )

    // The operation type and optional name inferred from the document.
    internal data class InferredOperation(
        val type: GraphQlOperationType,
        val name: String?
    )
}
