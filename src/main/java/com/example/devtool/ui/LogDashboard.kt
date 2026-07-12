package com.example.devtool.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.devtool.core.logging.DevLog
import com.example.devtool.core.logging.LogLevel
import com.example.devtool.core.logging.LoggerManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogDashboard() {
    val repository = LoggerManager.getRepository()
    val logs by repository.logs.collectAsState()
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf<LogLevel?>(null) }
    var selectedLog by remember { mutableStateOf<DevLog?>(null) }
    
    val filteredLogs = logs.filter { log ->
        val matchesQuery = searchQuery.isEmpty() || 
                log.message.contains(searchQuery, ignoreCase = true) || 
                log.tag.contains(searchQuery, ignoreCase = true)
        val matchesLevel = selectedLevel == null || log.level == selectedLevel
        matchesQuery && matchesLevel
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DevTool Dashboard") },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            repository.clearAll()
                        }
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear Logs")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                placeholder = { Text("Search logs...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                item {
                    AssistChip(
                        onClick = { selectedLevel = null },
                        label = { Text("ALL") },
                        modifier = Modifier.padding(end = 4.dp),
                        colors = if (selectedLevel == null) AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else AssistChipDefaults.assistChipColors()
                    )
                }
                items(LogLevel.values()) { level ->
                    AssistChip(
                        onClick = { selectedLevel = level },
                        label = { Text(level.name) },
                        modifier = Modifier.padding(end = 4.dp),
                        colors = if (selectedLevel == level) AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else AssistChipDefaults.assistChipColors()
                    )
                }
            }
            
            LogList(
                logs = filteredLogs,
                modifier = Modifier.weight(1f),
                onLogClick = { selectedLog = it }
            )
        }
    }

    selectedLog?.let { log ->
        AlertDialog(
            onDismissRequest = { selectedLog = null },
            title = { Text(log.tag) },
            text = { Text(log.message) },
            confirmButton = {
                TextButton(onClick = {
                    clipboardManager.setText(AnnotatedString("${log.tag}: ${log.message}"))
                    selectedLog = null
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Copy", modifier = Modifier.padding(start = 4.dp))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedLog = null }) {
                    Text("Close")
                }
            }
        )
    }
}
