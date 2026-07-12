package com.example.devtool.ui.dashboard.network

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
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
import com.example.devtool.network.model.NetworkCall
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkDetailScreen(
    call: NetworkCall,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Request", "Response")
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(formatFullLog(call)))
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy All")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> OverviewSection(call)
                1 -> RequestResponseSection(headers = call.requestHeaders, body = call.requestBody)
                2 -> RequestResponseSection(headers = call.responseHeaders, body = call.responseBody)
            }
        }
    }
}

@Composable
fun OverviewSection(call: NetworkCall) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        InfoRow("URL", call.url)
        InfoRow("Method", call.method)
        InfoRow("Status", "${call.statusCode} ${call.statusMessage}")
        InfoRow("Duration", "${call.duration}ms")
        InfoRow("Protocol", call.protocol)
        InfoRow("Timestamp", call.timestamp.toString())
        InfoRow("Request Size", "${call.requestSize} bytes")
        InfoRow("Response Size", "${call.responseSize} bytes")
    }
}

@Composable
fun RequestResponseSection(headers: Map<String, String>, body: String?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Headers", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        headers.forEach { (key, value) ->
            Row(modifier = Modifier.padding(bottom = 4.dp)) {
                Text("$key: ", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Text(value, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Body", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray.copy(alpha = 0.2f))
                .padding(8.dp)
        ) {
            Text(
                text = prettyPrintJson(body) ?: "No Body",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        Text(value, fontSize = 14.sp)
    }
}

fun prettyPrintJson(json: String?): String? {
    if (json == null) return null
    return try {
        JSONObject(json).toString(4)
    } catch (e: Exception) {
        try {
            JSONArray(json).toString(4)
        } catch (e2: Exception) {
            json
        }
    }
}

fun formatFullLog(call: NetworkCall): String {
    return buildString {
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
}
