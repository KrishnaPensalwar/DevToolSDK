package com.example.devtool.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.devtool.DevToolSdk
import kotlinx.coroutines.launch

@Composable
fun CacheScreen(
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var cachedResponses by remember { mutableStateOf<List<com.example.devtool.database.CachedResponseEntity>>(emptyList()) }
    var editItem by remember { mutableStateOf<com.example.devtool.database.CachedResponseEntity?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            cachedResponses = DevToolSdk.getAllCachedResponses()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(46.dp)) {
        Text(text = "Cached API Responses", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(cachedResponses) { item ->
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(text = "${item.method} ${item.url}")
                    Text(text = item.body, maxLines = 2)
                    Button(onClick = { editItem = item }) {
                        Text("Edit")
                    }
                }
            }
        }
        Button(onClick = onClose, modifier = Modifier.padding(top = 8.dp)) {
            Text("Back")
        }
    }

    if (editItem != null) {
        CacheEditDialog(
            cachedResponse = editItem!!,
            onDismiss = { editItem = null },
            onSave = { newBody ->
                scope.launch {
                    DevToolSdk.updateCachedResponse(editItem!!.url, editItem!!.method, newBody)
                    // Refresh list
                    cachedResponses = DevToolSdk.getAllCachedResponses()
                }
                editItem = null
            }
        )
    }
}
