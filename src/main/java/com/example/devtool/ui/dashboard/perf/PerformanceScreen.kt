package com.example.devtool.ui.dashboard.perf

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devtool.core.logging.DevLog
import com.example.devtool.core.logging.LogLevel
import com.example.devtool.core.logging.LoggerManager
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PerformanceScreen(modifier: Modifier = Modifier) {
    val repository = LoggerManager.getRepository()
    val allLogs by repository.logs.collectAsState()
    val perfLogs = allLogs.filter { it.level == LogLevel.PERFORMANCE }.take(50)

    Column(modifier = modifier.padding(16.dp)) {
        Text("Memory Usage History", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (perfLogs.isNotEmpty()) {
            MemoryChart(
                logs = perfLogs,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        } else {
            Box(modifier = Modifier.height(200.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Collecting data...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Detailed Snapshots", style = MaterialTheme.typography.titleMedium)
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(perfLogs) { log ->
                PerfItem(log = log)
            }
        }
    }
}

@Composable
fun MemoryChart(logs: List<DevLog>, modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    
    // Simple line chart representation
    Canvas(modifier = modifier) {
        if (logs.size < 2) return@Canvas
        
        val points = logs.map { log ->
            // Extract percentage from message: "Memory: 123 MB available of 456 MB (25.00%)"
            val percent = log.message.substringAfterLast("(").substringBefore("%").toFloatOrNull() ?: 0f
            percent
        }.reversed()
        
        val maxVal = 100f
        val width = size.width
        val height = size.height
        val stepX = width / (points.size - 1)
        
        val path = Path().apply {
            moveTo(0f, height - (points[0] / maxVal * height))
            points.forEachIndexed { index, value ->
                if (index > 0) {
                    lineTo(index * stepX, height - (value / maxVal * height))
                }
            }
        }
        
        drawPath(path, color = primaryColor, style = Stroke(width = 4f))
    }
}

@Composable
fun PerfItem(log: DevLog) {
    val date = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = log.message, fontSize = 13.sp)
                Text(text = date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
