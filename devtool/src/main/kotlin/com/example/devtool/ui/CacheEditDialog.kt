package com.example.devtool.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.devtool.database.CachedResponseEntity

@Composable
fun CacheEditDialog(
    cachedResponse: CachedResponseEntity,
    onDismiss: () -> Unit,
    onSave: (newBody: String) -> Unit
) {
    var editedBody by remember { mutableStateOf(cachedResponse.body) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onSave(editedBody) }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text(text = "Edit Cached Response") },
        text = {
            Column {
                Text(text = "${cachedResponse.method} ${cachedResponse.url}")
                TextField(
                    value = editedBody,
                    onValueChange = { editedBody = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    label = { Text("Body") }
                )
            }
        }
    )
}
