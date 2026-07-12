package com.example.devtool.ui.dashboard.network

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devtool.core.logging.LoggerManager
import com.example.devtool.network.model.NetworkCall
import com.example.devtool.ui.components.*
import com.example.devtool.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkListScreen(modifier: Modifier = Modifier) {
    val repository = LoggerManager.getNetworkRepository()
    val calls by repository.calls.collectAsState()
    val scope = rememberCoroutineScope()
    var selectedCall by remember { mutableStateOf<NetworkCall?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf<String?>(null) }
    val filters = listOf("ALL", "GET", "POST", "PUT", "DELETE", "PATCH")

    val filteredCalls = calls.filter { call ->
        val matchesQuery = searchQuery.isEmpty() ||
                call.url.contains(searchQuery, ignoreCase = true) ||
                call.endpoint.contains(searchQuery, ignoreCase = true)
        val matchesMethod = selectedMethod == null || selectedMethod == "ALL" ||
                call.method.equals(selectedMethod, ignoreCase = true)
        matchesQuery && matchesMethod
    }

    val successCount = calls.count { it.statusCode in 200..299 }
    val failedCount = calls.count { it.statusCode >= 400 }

    if (selectedCall != null) {
        NetworkDetailScreen(
            call = selectedCall!!,
            onDismiss = { selectedCall = null },
            modifier = modifier
        )
        return
    }

    Column(modifier = modifier.background(MaterialTheme.colorScheme.background)) {

        // Sticky filter header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(0.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Network Activity",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = colorStatusSuccess,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$successCount OK",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (failedCount > 0) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = colorStatusError,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "$failedCount Failed",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorStatusError
                            )
                        }
                    }
                    IconButton(
                        onClick = { scope.launch { repository.clearAll() } },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear All",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            DevToolSearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Filter URL or endpoint...",
                modifier = Modifier.fillMaxWidth()
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = if (filter == "ALL") selectedMethod == null else selectedMethod == filter
                    DevToolFilterChip(
                        label = filter,
                        selected = isSelected,
                        onClick = {
                            selectedMethod = if (filter == "ALL") null else filter
                        }
                    )
                }
            }
        }

        if (filteredCalls.isEmpty()) {
            EmptyStateView(
                message = "No network calls",
                subtitle = "Make an HTTP request to see it here"
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredCalls) { call ->
                    NetworkCallItem(call = call, onClick = { selectedCall = call })
                }
            }
        }
    }
}

@Composable
fun NetworkCallItem(call: NetworkCall, onClick: () -> Unit) {
    val statusDotColor = when {
        call.statusCode in 200..299 -> colorStatusSuccess
        call.statusCode in 400..599 -> colorStatusError
        else -> colorStatusNeutral
    }
    val statusTextColor = when {
        call.statusCode in 200..299 -> MaterialTheme.colorScheme.primary
        call.statusCode in 400..599 -> colorStatusError
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val methodColor = methodColor(call.method)
    val statusLabel = when {
        call.statusCode in 200..299 -> "${call.statusCode} OK"
        call.statusCode in 400..499 -> "${call.statusCode} Auth"
        call.statusCode >= 500 -> "${call.statusCode} Error"
        else -> call.statusCode.toString()
    }
    val sizeLabel = formatSize(call.responseSize)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(logRowBackground)
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(0.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatusDot(color = statusDotColor)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = call.endpoint.ifEmpty { call.url },
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = call.method,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = methodColor
                )
                Text(
                    text = call.host,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusTextColor
                )
                Text(
                    text = "${call.duration}ms • $sizeLabel",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun formatSize(bytes: Long): String = when {
    bytes >= 1_048_576 -> String.format("%.1fMB", bytes / 1_048_576.0)
    bytes >= 1024 -> String.format("%.1fkB", bytes / 1024.0)
    else -> "${bytes}B"
}

fun methodColor(method: String): Color = when (method.uppercase()) {
    "GET"    -> methodGet
    "POST"   -> methodPost
    "PUT"    -> methodPut
    "DELETE" -> methodDelete
    "PATCH"  -> methodPatch
    else     -> colorStatusNeutral
}
