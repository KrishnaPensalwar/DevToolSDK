package com.example.devtool.network

import com.example.devtool.network.model.NetworkCall

data class EndpointStats(
    val endpoint: String,
    val method: String,
    val totalCalls: Int,
    val avgDuration: Long,
    val minDuration: Long,
    val maxDuration: Long,
    val avgResponseSize: Long,
    val successRate: Float,
    val lastCalled: Long
)

object AnalyticsCalculator {
    fun calculate(calls: List<NetworkCall>): List<EndpointStats> {
        return calls.groupBy { "${it.method} ${it.endpoint}" }
            .map { (key, group) ->
                val total = group.size
                val durations = group.map { it.duration }
                val successes = group.count { it.success }
                val last = group.maxOf { it.timestamp }
                
                EndpointStats(
                    endpoint = group.first().endpoint,
                    method = group.first().method,
                    totalCalls = total,
                    avgDuration = durations.average().toLong(),
                    minDuration = durations.minOrNull() ?: 0L,
                    maxDuration = durations.maxOrNull() ?: 0L,
                    avgResponseSize = group.map { it.responseSize }.average().toLong(),
                    successRate = (successes.toFloat() / total) * 100f,
                    lastCalled = last
                )
            }
    }
}
