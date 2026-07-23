package io.github.krishnapensalwar.devkit.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.krishnapensalwar.devkit.DevToolSdk
import io.github.krishnapensalwar.devkit.database.CachedResponseEntity
import kotlinx.coroutines.launch

@Composable
fun CacheScreen(
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var cachedResponses by remember { mutableStateOf<List<CachedResponseEntity>>(emptyList()) }
    var editItem by remember { mutableStateOf<CachedResponseEntity?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            cachedResponses = DevToolSdk.getAllCachedResponses()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(46.dp)) {
        Text(text = "Cached API Responses", style = MaterialTheme.typography.headlineMedium)
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
            onSave = { newBody: String ->
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
