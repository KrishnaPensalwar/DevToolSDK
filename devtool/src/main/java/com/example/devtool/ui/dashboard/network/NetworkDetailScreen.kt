package com.example.devtool.ui.dashboard.network

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.devtool.network.model.NetworkCall
import com.example.devtool.ui.dashboard.network.components.OverviewContent
import com.example.devtool.ui.dashboard.network.components.RequestContent
import com.example.devtool.ui.dashboard.network.components.ResponseContent
import com.example.devtool.ui.theme.*

enum class DetailTab { OVERVIEW, REQUEST, RESPONSE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkDetailScreen(
    call: NetworkCall,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableStateOf(DetailTab.OVERVIEW) }
    var fullImageUrl by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier,
        containerColor = sdkBackground,
        topBar = {
            Column(modifier = Modifier.background(sdkBackground)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(sdkSurface)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = sdkOnSurface
                        )
                    }
                    Text(
                        text = "Traffic Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = sdkOnSurface
                    )
                    Row {
                        IconButton(
                            onClick = { /* Refresh */ },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(sdkSurface)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = sdkOnSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(formatFullLog(call)))
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(sdkSurface)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = sdkOnSurface
                            )
                        }
                    }
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(sdkSurface)
                        .padding(4.dp)
                ) {
                    DetailTab.entries.forEach { tab ->
                        val selected = selectedTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) sdkSurfaceVariant else Color.Transparent)
                                .clickable { selectedTab = tab }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontSize = 14.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) sdkOnSurface else sdkOnSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            when (selectedTab) {
                DetailTab.OVERVIEW -> OverviewContent(call)
                DetailTab.REQUEST -> RequestContent(call, onImageClick = { fullImageUrl = it })
                DetailTab.RESPONSE -> ResponseContent(call, onImageClick = { fullImageUrl = it })
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    fullImageUrl?.let { image ->
        Dialog(onDismissRequest = { fullImageUrl = null }) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.9f)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().clickable { fullImageUrl = null }) {
                    AsyncImage(
                        model = image,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    )
                    IconButton(
                        onClick = { fullImageUrl = null },
                        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
        }
    }
}

fun formatFullLog(call: NetworkCall): String = buildString {
    appendLine("URL: ${call.url}")
    appendLine("Method: ${call.method}")
    appendLine("Status: ${call.statusCode}")
    appendLine("Duration: ${call.duration}ms")
    appendLine("\n--- Request Headers ---")
    call.requestHeaders.forEach { appendLine("${it.key}: ${it.value}") }
    appendLine("\n--- Request Body ---")
    appendLine(call.requestBody ?: "None")
    appendLine("\n--- Response Headers ---")
    call.responseHeaders.forEach { appendLine("${it.key}: ${it.value}") }
    appendLine("\n--- Response Body ---")
    appendLine(call.responseBody ?: "None")
}
