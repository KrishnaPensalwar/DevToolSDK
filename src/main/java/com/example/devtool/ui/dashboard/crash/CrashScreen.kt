package com.example.devtool.ui.dashboard.crash

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devtool.core.logging.DevLog
import com.example.devtool.core.logging.LogLevel
import com.example.devtool.core.logging.LoggerManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CrashScreen(modifier: Modifier = Modifier) {
    val repository = LoggerManager.getRepository()
    val allLogs by repository.logs.collectAsState()
    val crashes = allLogs.filter { it.level == LogLevel.CRASH }
    val scope = rememberCoroutineScope()
    var selectedCrash by remember { mutableStateOf<DevLog?>(null) }
    val clipboardManager = LocalClipboardManager.current

    Column(modifier = modifier) {
        if (crashes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No crashes detected", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            title = { Text("Crash Details") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = crash.message,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    clipboardManager.setText(AnnotatedString(crash.message))
                    selectedCrash = null
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
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
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "CRASH",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 12.sp
                )
                Text(
                    text = date,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = crash.message.substringBefore("\n"),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer,
                maxLines = 2
            )
        }
    }
}
