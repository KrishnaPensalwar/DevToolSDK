package com.example.devtool.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devtool.core.logging.LoggerManager
import com.example.devtool.ui.dashboard.network.components.NetworkSummaryBadge
import com.example.devtool.ui.theme.*

@Composable
fun DashboardHomeScreen(modifier: Modifier = Modifier) {
    val networkRepo = LoggerManager.getNetworkRepository()
    val logsRepo = LoggerManager.getRepository()
    
    val networkCalls by networkRepo.calls.collectAsState()
    val allLogs by logsRepo.logs.collectAsState()
    
    val crashesCount = allLogs.count { it.level.name == "CRASH" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(sdkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Network Snapshot", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = sdkPrimary)
            NetworkSummaryBadge(calls = networkCalls)
        }
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Health & Stability", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = sdkPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MetricCard(
                    title = "Crashes",
                    value = crashesCount.toString(),
                    icon = Icons.Default.BugReport,
                    modifier = Modifier.weight(1f),
                    color = if (crashesCount > 0) colorStatusError else colorStatusNeutral
                )
                MetricCard(
                    title = "API Failures",
                    value = networkCalls.count { !it.success }.toString(),
                    icon = Icons.Default.Error,
                    modifier = Modifier.weight(1f),
                    color = if (networkCalls.any { !it.success }) colorStatusError else colorStatusNeutral
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = sdkSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = sdkOnSurfaceVariant)
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = sdkOnSurface)
        }
    }
}
