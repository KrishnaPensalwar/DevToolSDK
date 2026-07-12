package com.example.devtool.ui.dashboard.network

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

    if (selectedCall != null) {
        NetworkDetailScreen(
            call = selectedCall!!,
            onDismiss = { selectedCall = null },
            modifier = modifier
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Network Inspector") },
                    actions = {
                        IconButton(onClick = { scope.launch { repository.clearAll() } }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                )
            },
            modifier = modifier
        ) { paddingValues ->
            LazyColumn(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                items(calls) { call ->
                    NetworkCallItem(call = call, onClick = { selectedCall = call })
                }
            }
        }
    }
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
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = call.method,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = call.endpoint,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
            }
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${call.statusCode} ${call.statusMessage}",
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " • ${call.duration}ms",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Text(
                    text = " • ${call.host}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp),
                    maxLines = 1
                )
            }
        }
    }
}
