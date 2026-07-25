package io.github.krishnapensalwar.devkit.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.krishnapensalwar.devkit.DevToolSdk
import io.github.krishnapensalwar.devkit.database.CachedResponseEntity
import io.github.krishnapensalwar.devkit.ui.dashboard.network.ResponseEditorScreen
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp)
            ) {
                Text(
                    text = "Cached API Responses",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (cachedResponses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(
                            text = "No cached API responses found.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(cachedResponses) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.method.uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Status: ${item.status}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.url,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 2,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Text(
                                        text = item.body,
                                        fontSize = 11.sp,
                                        maxLines = 3,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                    Button(
                                        onClick = { editItem = item },
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text("Edit Response", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Back")
                }
            }
        }
    }

    if (editItem != null) {
        ResponseEditorScreen(
            initialBody = editItem!!.body,
            endpoint = "${editItem!!.method} ${editItem!!.url}",
            onDismiss = { editItem = null },
            onSave = { newBody ->
                scope.launch {
                    DevToolSdk.updateCachedResponse(editItem!!.url, editItem!!.method, newBody)
                    cachedResponses = DevToolSdk.getAllCachedResponses()
                }
                editItem = null
            }
        )
    }
}
