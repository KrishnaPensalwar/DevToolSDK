package com.example.devtool.ui.dashboard.network.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devtool.network.model.NetworkCall
import com.example.devtool.ui.theme.*

private data class SummaryItem(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun NetworkSummaryBadge(calls: List<NetworkCall>) {
    val items = remember(calls) {
        val total = calls.size
        val success = calls.count { it.success }
        val failed = total - success
        val avg = if (total > 0) calls.map { it.duration }.average().toLong() else 0L
        val data = calls.sumOf { it.responseSize }

        listOf(
            SummaryItem("Total", total.toString(), Icons.Default.CloudDownload, sdkPrimary),
            SummaryItem("Success", success.toString(), Icons.Default.CheckCircle, colorStatusSuccess),
            SummaryItem("Failed", failed.toString(), Icons.Default.Error, colorStatusError),
            SummaryItem("Avg Time", "${avg}ms", Icons.Default.Schedule, colorStatusWarning),
            SummaryItem("Total Data", "${data / 1024}KB", Icons.Default.Storage, methodPatch)
        )
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = sdkSurface),
                modifier = Modifier.width(130.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Surface(
                        color = item.color.copy(alpha = 0.1f),
                        shape = CircleShape,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(item.icon, contentDescription = null, tint = item.color, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(item.title, fontSize = 11.sp, color = sdkOnSurfaceVariant)
                    Text(item.value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = sdkOnSurface)
                }
            }
        }
    }
}
