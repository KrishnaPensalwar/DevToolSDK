package io.github.krishnapensalwar.devkit

import io.ktor.client.call.HttpClientCall
import io.ktor.client.plugins.observer.wrapWithContent
import io.ktor.client.statement.HttpResponse
import io.ktor.http.content.OutgoingContent
import io.ktor.util.AttributeKey
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.readRemaining

internal val CapturedResponseBodyKey = AttributeKey<String>("DevToolCapturedResponseBody")

internal fun outgoingContentAsText(content: OutgoingContent): String? {
    return when (content) {
        is OutgoingContent.ByteArrayContent -> {
            try {
                String(content.bytes(), Charsets.UTF_8)
            } catch (_: Exception) {
                null
            }
        }
        is OutgoingContent.NoContent -> null
        is OutgoingContent.WriteChannelContent -> "(write channel content omitted)"
        is OutgoingContent.ReadChannelContent -> "(read channel content omitted)"
        is OutgoingContent.ProtocolUpgrade -> "(protocol upgrade content omitted)"
        else -> null
    }
}

/**
 * Reads the engine body channel once, stores the text on the call, and replaces the body with a
 * fresh in-memory channel so the app / ContentNegotiation can still consume it.
 *
 * Do not use [io.ktor.client.statement.bodyAsText] or [io.ktor.client.statement.bodyAsChannel] here:
 * those go through the receive pipeline, complete the call Job, and cause
 * "Parent job is completed" on the next reader.
 */
@OptIn(InternalAPI::class)
internal suspend fun HttpResponse.snapshotBodyForApp(): HttpResponse {
    if (call.attributes.contains(CapturedResponseBodyKey)) return this
    val bytes = try {
        rawContent.readRemaining().readBytes()
    } catch (_: Throwable) {
        // Never replace the live body with empty bytes — that makes ContentNegotiation
        // throw JsonConvertException (EOF) on the next reader.
        return this
    }
    val text = bytes.decodeToString()
    call.attributes.put(CapturedResponseBodyKey, text)
    if (bytes.isEmpty()) {
        // Channel already empty (true empty response or prior drain). Skip wrapWithContent.
        return this
    }
    val wrapped: HttpClientCall = call.wrapWithContent(ByteReadChannel(bytes))
    wrapped.attributes.put(CapturedResponseBodyKey, text)
    return wrapped.response
}
