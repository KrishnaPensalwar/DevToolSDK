package io.github.krishnapensalwar.devkit.mock.identity

enum class RequestProtocol {
    REST,
    GRAPHQL
}

enum class GraphQlOperationType {
    QUERY,
    MUTATION,
    SUBSCRIPTION
}

data class RequestIdentity(
    val protocol: RequestProtocol,
    val method: String,
    val url: String,
    val endpoint: String,
    val identityKey: String,
    val displayName: String,
    val graphQlOperationType: GraphQlOperationType? = null,
    val graphQlOperationName: String? = null,
    val graphQlDocumentHash: String? = null
) {
    val isGraphQl: Boolean get() = protocol == RequestProtocol.GRAPHQL

    companion object {
        fun parse(
            url: String,
            method: String,
            requestBody: String?,
            contentType: String? = null,
            queryParams: Map<String, String> = emptyMap()
        ): RequestIdentity {
            val graphQl = GraphQlParser.parse(
                url = url,
                method = method,
                requestBody = requestBody,
                contentType = contentType,
                queryParams = queryParams
            )
            if (graphQl != null) return graphQl
            return rest(url, method)
        }

        fun rest(url: String, method: String): RequestIdentity {
            val normalizedMethod = method.uppercase()
            return RequestIdentity(
                protocol = RequestProtocol.REST,
                method = normalizedMethod,
                url = url,
                endpoint = pathOf(url),
                identityKey = restKey(normalizedMethod, url),
                displayName = "$normalizedMethod ${pathOf(url)}"
            )
        }

        fun restKey(method: String, url: String): String = "rest|${method.uppercase()}|$url"

        fun pathOf(url: String): String {
            val path = url.substringAfter("://", url)
                .substringAfter("/", "")
                .substringBefore("?")

            return if (path.isEmpty()) "/" else "/$path"
        }
    }
}
