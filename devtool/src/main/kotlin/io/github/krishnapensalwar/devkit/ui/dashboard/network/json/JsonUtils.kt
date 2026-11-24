package io.github.krishnapensalwar.devkit.ui.dashboard.network.json

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

/** One highlighted occurrence: [occurrence]-th hit inside the key or value text of the node at [path]. */
data class JsonMatch(val path: List<String>, val inKey: Boolean, val occurrence: Int)

@Stable
class JsonSearchState(val matches: List<JsonMatch>) {
    var index by mutableIntStateOf(0)
        private set
    val active: JsonMatch? get() = matches.getOrNull(index)

    fun next() {
        if (matches.isNotEmpty()) index = (index + 1) % matches.size
    }

    fun previous() {
        if (matches.isNotEmpty()) index = (index - 1 + matches.size) % matches.size
    }
}

@Composable
fun rememberJsonSearch(body: String?, query: String): JsonSearchState =
    remember(body, query) { JsonSearchState(findJsonMatches(body, query)) }

fun parseJson(text: String?): Any? {
    if (text.isNullOrBlank()) return null
    return runCatching { JSONObject(text) }.getOrNull()
        ?: runCatching { JSONArray(text) }.getOrNull()
}

fun formatJsonKey(key: String): String =
    if (key.startsWith("[") && key.endsWith("]")) "$key: " else "\"$key\": "

fun isJsonNull(value: Any?): Boolean =
    value == null || value == JSONObject.NULL || value.toString() == "null"

fun formatJsonValue(value: Any?): String = when {
    isJsonNull(value) -> "null"
    value is String || value is CharSequence -> "\"$value\""
    else -> value.toString()
}

fun occurrencesOf(text: String, query: String): List<Int> {
    if (query.isEmpty()) return emptyList()
    val result = mutableListOf<Int>()
    var i = text.indexOf(query, ignoreCase = true)
    while (i >= 0) {
        result += i
        i = text.indexOf(query, i + query.length, ignoreCase = true)
    }
    return result
}

/** Matches in the same order the tree viewer renders them, so next/previous walks top to bottom. */
fun findJsonMatches(body: String?, query: String): List<JsonMatch> {
    if (body.isNullOrBlank() || query.isBlank()) return emptyList()
    val out = mutableListOf<JsonMatch>()

    fun add(text: String, path: List<String>, inKey: Boolean) {
        occurrencesOf(text, query).indices.forEach { out += JsonMatch(path, inKey, it) }
    }

    fun visit(key: String, value: Any, path: List<String>) {
        when (value) {
            is JSONObject -> {
                add(key, path, inKey = true)
                value.keys().forEach { visit(it, value.get(it), path + it) }
            }
            is JSONArray -> {
                add(key, path, inKey = true)
                for (i in 0 until value.length()) visit("[$i]", value.get(i), path + "[$i]")
            }
            else -> {
                add(formatJsonKey(key), path, inKey = true)
                add(formatJsonValue(value), path, inKey = false)
            }
        }
    }

    when (val root = parseJson(body)) {
        is JSONObject -> root.keys().forEach { visit(it, root.get(it), listOf(it)) }
        is JSONArray -> for (i in 0 until root.length()) visit("[$i]", root.get(i), listOf("[$i]"))
        else -> add(body, emptyList(), inKey = false)
    }
    return out
}

fun JsonMatch?.occurrenceAt(path: List<String>, inKey: Boolean): Int =
    if (this != null && this.path == path && this.inKey == inKey) occurrence else -1

fun JsonMatch?.isInside(path: List<String>): Boolean =
    this != null && this.path.size > path.size && this.path.subList(0, path.size) == path

fun jsonContains(
    value: Any,
    query: String
): Boolean {

    if (query.isBlank()) return true

    return when (value) {

        is JSONObject -> {
            value.keys().asSequence().any { key ->
                key.contains(query, ignoreCase = true) ||
                        jsonContains(value.get(key), query)
            }
        }

        is JSONArray -> {
            (0 until value.length()).any {
                jsonContains(value.get(it), query)
            }
        }

        else -> {
            value.toString().contains(query, ignoreCase = true)
        }
    }
}

fun isImageUrl(text: String?): Boolean {

    if (text == null) return false

    return text.startsWith("http", ignoreCase = true) &&
            (
                    text.endsWith(".png", true) ||
                            text.endsWith(".jpg", true) ||
                            text.endsWith(".jpeg", true) ||
                            text.endsWith(".gif", true) ||
                            text.endsWith(".webp", true) ||
                            text.endsWith(".svg", true)
                    )
}