package com.example.devtool.ui.dashboard.network

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devtool.core.logging.LoggerManager
import com.example.devtool.network.model.NetworkCall
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkListScreen(modifier: Modifier = Modifier) {
    val repository = LoggerManager.getNetworkRepository()
    val calls by repository.calls.collectAsState()
    val scope = rememberCoroutineScope()
    var selectedCall by remember { mutableStateOf<NetworkCall?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf<String?>(null) }
    val methods = listOf("GET", "POST", "PUT", "DELETE", "PATCH")

    val filteredCalls = calls.filter { call ->
        val matchesQuery = searchQuery.isEmpty() ||
                call.url.contains(searchQuery, ignoreCase = true) ||
                call.endpoint.contains(searchQuery, ignoreCase = true)
        val matchesMethod =
            selectedMethod == null || call.method.equals(selectedMethod, ignoreCase = true)
        matchesQuery && matchesMethod
    }

    if (selectedCall != null) {
        NetworkDetailScreen(
            call = selectedCall!!,
            onDismiss = { selectedCall = null },
            modifier = modifier
        )
    } else {
//        Scaffold(
//            topBar = {
//                TopAppBar(
//                    title = { Text("Network Inspector") },
//                    actions = {
//                        IconButton(onClick = { scope.launch { repository.clearAll() } }) {
//                            Icon(Icons.Default.Clear, contentDescription = "Clear")
//                        }
//                    },
//                    modifier = Modifier.height(50.dp),
//                    windowInsets = WindowInsets(0.dp, top = 10.dp)
//                )
//            },
//            modifier = modifier
//        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(top = 60.dp)
                    .fillMaxSize()
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Network Inspector")
                    IconButton(onClick = { scope.launch { repository.clearAll() } }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }

                }
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    placeholder = { Text("Search by URL or Endpoint") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedMethod == null,
                            onClick = { selectedMethod = null },
                            label = { Text("ALL") }
                        )
                    }
                    items(methods) { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            label = { Text(method) }
                        )
                    }
                }

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredCalls) { call ->
                        NetworkCallItem(call = call, onClick = { selectedCall = call })
                    }
                }
            }
        }
//    }
}

@Composable
fun NetworkCallItem(call: NetworkCall, onClick: () -> Unit) {
    val statusColor = when {
        call.statusCode in 200..299 -> Color(0xFF2E7D32)
        call.statusCode in 400..599 -> Color.Red
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = call.method,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = call.endpoint,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    maxLines = 1
                )
            }
            Row(
                modifier = Modifier.padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = call.statusCode.toString(),
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " • ${call.duration}ms",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = " • ${call.host}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .weight(1f),
                    maxLines = 1
                )
            }
        }
    }
}
