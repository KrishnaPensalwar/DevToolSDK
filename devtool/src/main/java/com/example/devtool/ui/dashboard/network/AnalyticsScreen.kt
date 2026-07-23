package com.example.devtool.ui.dashboard.network

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devtool.core.logging.LoggerManager
import com.example.devtool.network.AnalyticsCalculator
import com.example.devtool.network.EndpointStats
import com.example.devtool.ui.dashboard.network.methodColor
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnalyticsScreen(modifier: Modifier = Modifier) {
    val repository = LoggerManager.getNetworkRepository()
    val calls by repository.calls.collectAsState()
    val stats = remember(calls) { AnalyticsCalculator.calculate(calls) }
    
    var sortBy by remember { mutableStateOf(SortType.MOST_CALLED) }

    val sortedStats = remember(stats, sortBy) {
        when (sortBy) {
            SortType.MOST_CALLED -> stats.sortedByDescending { it.totalCalls }
            SortType.SLOWEST -> stats.sortedByDescending { it.avgDuration }
            SortType.LARGEST -> stats.sortedByDescending { it.avgResponseSize }
            SortType.FAILURES -> stats.sortedBy { it.successRate }
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SortType.values().forEach { type ->
                FilterChip(
                    selected = sortBy == type,
                    onClick = { sortBy = type },
                    label = { Text(type.label, fontSize = 10.sp) }
                )
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(sortedStats) { item ->
                EndpointStatsItem(item)
            }
        }
    }
}

@Composable
fun EndpointStatsItem(stats: EndpointStats) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stats.method,
                    fontWeight = FontWeight.Bold,
                    color = methodColor(stats.method),
                    fontSize = 12.sp
                )
                Text(
                    text = stats.endpoint,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp).weight(1f),
                    maxLines = 1
                )
                Text(
                    text = "${stats.totalCalls} calls",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatColumn("Avg Dur", "${stats.avgDuration}ms")
                StatColumn("Success", "${stats.successRate.toInt()}%")
                StatColumn("Avg Size", "${stats.avgResponseSize}B")
                StatColumn("Last", SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(stats.lastCalled)))
            }
        }
    }
}

@Composable
fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

enum class SortType(val label: String) {
    MOST_CALLED("Calls"),
    SLOWEST("Slow"),
    LARGEST("Size"),
    FAILURES("Failures")
}
