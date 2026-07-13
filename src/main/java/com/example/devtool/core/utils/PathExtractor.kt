package com.example.devtool.core.utils

import android.net.Uri

object ApiNameExtractor {

    private val ignoredSegments = setOf(
        "api", "apis",
        "v1", "v2", "v3", "v4", "v5",
        "rest", "graphql",
        "public", "private", "internal",
        "mobile", "android", "ios"
    )

    fun extract(url: String): String {
        val pathSegments = Uri.parse(url)
            .pathSegments
            .filter { it.isNotBlank() }

        if (pathSegments.isEmpty()) return "Unknown"

        val candidates = pathSegments
            .filterNot(::isIgnored)
            .mapIndexed { index, segment ->
                Candidate(
                    value = segment,
                    score = score(segment, index, pathSegments.size)
                )
            }

        if (candidates.isEmpty()) {
            return prettify(pathSegments.last())
        }

        return prettify(candidates.maxBy { it.score }.value)
    }

    private fun score(
        segment: String,
        index: Int,
        totalSegments: Int
    ): Int {
        var score = 0

        val lower = segment.lowercase()

        // Prefer alphabetic words
        if (lower.matches(Regex("[a-z][a-z0-9_-]*"))) {
            score += 30
        }

        // Prefer longer meaningful words
        score += lower.length.coerceAtMost(15)

        // Prefer segments near the end
        score += index * 10

        // Bonus for action-like names
        if (lower in setOf(
                "login",
                "logout",
                "search",
                "rank",
                "profile",
                "details",
                "summary",
                "history",
                "refresh",
                "upload",
                "download",
                "verify"
            )
        ) {
            score += 40
        }

        // Penalize generic resource names
        if (lower in setOf(
                "users",
                "user",
                "orders",
                "order",
                "customers",
                "customer",
                "products",
                "product"
            )
        ) {
            score -= 10
        }

        // Prefer leaf nodes
        if (index == totalSegments - 1) {
            score += 25
        }

        return score
    }

    private fun isIgnored(segment: String): Boolean {
        val lower = segment.lowercase()

        return lower in ignoredSegments ||
                lower.matches(Regex("\\d+")) ||                       // numeric id
                lower.matches(Regex("[0-9a-fA-F-]{8,}")) ||           // uuid/hash
                lower.contains(".")                                  // file extension
    }

    private fun prettify(value: String): String =
        value
            .replace("-", " ")
            .replace("_", " ")
            .split(" ")
            .joinToString(" ") {
                it.replaceFirstChar(Char::uppercase)
            }

    private data class Candidate(
        val value: String,
        val score: Int
    )
}