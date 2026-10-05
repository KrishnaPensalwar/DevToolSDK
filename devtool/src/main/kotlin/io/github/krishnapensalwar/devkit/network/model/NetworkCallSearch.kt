package io.github.krishnapensalwar.devkit.network.model

import io.github.krishnapensalwar.devkit.mock.identity.RequestIdentity

internal fun NetworkCall.matchesQuery(query: String, identity: RequestIdentity): Boolean {
    if (query.isBlank()) return true
    val q = query.trim()
    return url.contains(q, ignoreCase = true) ||
        host.contains(q, ignoreCase = true) ||
        endpoint.contains(q, ignoreCase = true) ||
        method.contains(q, ignoreCase = true) ||
        identity.displayName.contains(q, ignoreCase = true) ||
        identity.graphQlOperationName.orEmpty().contains(q, ignoreCase = true) ||
        identity.graphQlOperationType?.name.orEmpty().contains(q, ignoreCase = true) ||
        requestBody.orEmpty().contains(q, ignoreCase = true)
}
