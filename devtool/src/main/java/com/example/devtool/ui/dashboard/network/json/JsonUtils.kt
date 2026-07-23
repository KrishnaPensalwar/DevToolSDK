package com.example.devtool.ui.dashboard.network.json

import org.json.JSONArray
import org.json.JSONObject

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

fun extractImageUrls(body: String?): List<String> {

    if (body == null) return emptyList()

    val regex =
        "(https?://\\S+\\.(?:png|jpg|jpeg|gif|webp|svg))"
            .toRegex(RegexOption.IGNORE_CASE)

    return regex
        .findAll(body)
        .map { it.value }
        .distinct()
        .toList()
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