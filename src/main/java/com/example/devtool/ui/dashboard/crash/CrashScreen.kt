package com.example.devtool.ui.dashboard.crash

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devtool.core.logging.DevLog
import com.example.devtool.core.logging.LogLevel
import com.example.devtool.core.logging.LoggerManager
import com.example.devtool.ui.components.EmptyStateView
import com.example.devtool.ui.components.StatusDot
import com.example.devtool.ui.theme.colorStatusError
import com.example.devtool.ui.theme.logRowBackground
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CrashScreen(modifier: Modifier = Modifier) {
    val repository = LoggerManager.getRepository()
    val allLogs by repository.logs.collectAsState()
    val crashes = allLogs.filter { it.level == LogLevel.CRASH }
    var selectedCrash by remember { mutableStateOf<DevLog?>(null) }
    val clipboardManager = LocalClipboardManager.current

    Column(modifier = modifier.background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Crash Reports",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${crashes.size} events",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)

        if (crashes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.BugReport,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EmptyStateView(
                        message = "No crashes detected",
                        subtitle = "App is running smoothly"
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(crashes) { crash ->
                    CrashItem(crash = crash, onClick = { selectedCrash = crash })
                }
            }
        }
    }

    selectedCrash?.let { crash ->
        AlertDialog(
            onDismissRequest = { selectedCrash = null },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusDot(color = colorStatusError)
                    Text(
                        text = "Crash Details",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .background(
                            MaterialTheme.colorScheme.background,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = crash.message,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    clipboardManager.setText(AnnotatedString(crash.message))
                    selectedCrash = null
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Copy", modifier = Modifier.padding(start = 4.dp))
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedCrash = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun CrashItem(crash: DevLog, onClick: () -> Unit) {
    val date = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault()).format(Date(crash.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(logRowBackground)
            .border(
                width = 0.5.dp,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.05f)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatusDot(color = colorStatusError, modifier = Modifier.padding(top = 4.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(colorStatusError.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "CRASH",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = colorStatusError,
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = date,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = crash.message.substringBefore("\n"),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
        }
    }
}
