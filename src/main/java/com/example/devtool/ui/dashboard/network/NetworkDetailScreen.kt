package com.example.devtool.ui.dashboard.network

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
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
                title = { Text(call.endpoint.ifEmpty { "Details" }, maxLines = 1) },
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
        Column(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()) {
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
                2 -> RequestResponseSection(
                    headers = call.responseHeaders,
                    body = call.responseBody
                )
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
    var searchQuery by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            placeholder = { Text("Search...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true
        )

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            ExpandableHeaders(headers, searchQuery)

            Spacer(modifier = Modifier.height(16.dp))
            Text("Body", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray.copy(alpha = 0.2f))
                    .padding(8.dp)
            ) {
                ExpandableJsonViewer(body, searchQuery)
            }
        }
    }

    ExpandableHeaders(headers)

    Spacer(modifier = Modifier.height(16.dp))
    Text("Body", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray.copy(alpha = 0.2f))
            .padding(8.dp)
    ) {
        ExpandableJsonViewer(body)
    }
}


@Composable
fun ExpandableHeaders(headers: Map<String, String>, searchQuery: String = "") {
    val filteredHeaders = if (searchQuery.isBlank()) {
        headers
    } else {
        headers.filter {
            it.key.contains(searchQuery, ignoreCase = true) || it.value.contains(
                searchQuery,
                ignoreCase = true
            )
        }
    }

    var expanded by remember { mutableStateOf(false) }
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank() && filteredHeaders.isNotEmpty()) expanded = true
    }

    if (filteredHeaders.isEmpty() && searchQuery.isNotBlank()) return

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Headers (${filteredHeaders.size}/${headers.size})", fontWeight = FontWeight.Bold)
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand Headers"
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)) {
                filteredHeaders.forEach { (key, value) ->
                    Row(modifier = Modifier.padding(bottom = 4.dp)) {
                        HighlightedText(
                            text = "$key: ",
                            query = searchQuery,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        HighlightedText(text = value, query = searchQuery, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableJsonViewer(jsonString: String?, searchQuery: String = "") {
    if (jsonString.isNullOrBlank()) {
        Text("No Body", fontSize = 12.sp)
        return
    }

    val jsonObj = try {
        JSONObject(jsonString)
    } catch (e: Exception) {
        null
    }
    val jsonArr = try {
        if (jsonObj == null) JSONArray(jsonString) else null
    } catch (e: Exception) {
        null
    }

    if (jsonObj != null) {
        JsonObjectViewer(jsonObj, rootName = "Root", searchQuery = searchQuery)
    } else if (jsonArr != null) {
        JsonArrayViewer(jsonArr, rootName = "Root", searchQuery = searchQuery)
    } else {
        HighlightedText(text = jsonString, query = searchQuery, fontSize = 12.sp)
    }
}

@Composable
fun JsonObjectViewer(jsonObject: JSONObject, rootName: String? = null, searchQuery: String = "") {
    val keys = jsonObject.keys().asSequence().toList()
    val filteredKeys = if (searchQuery.isBlank()) keys else keys.filter { key ->
        key.contains(searchQuery, ignoreCase = true) || jsonContains(
            jsonObject.get(key),
            searchQuery
        )
    }

    if (filteredKeys.isEmpty() && searchQuery.isNotBlank()) return

    if (rootName == null) {
        Column {
            filteredKeys.forEach { key ->
                JsonNodeViewer(key, jsonObject.get(key), searchQuery)
            }
        }
    } else {
        var expanded by remember { mutableStateOf(false) }
        LaunchedEffect(searchQuery) {
            if (searchQuery.isNotBlank() && filteredKeys.isNotEmpty()) expanded = true
        }
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                HighlightedText(
                    text = rootName,
                    query = searchQuery,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(" {${filteredKeys.size}/${keys.size}}", fontSize = 12.sp, color = Color.Gray)
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    filteredKeys.forEach { key ->
                        JsonNodeViewer(key, jsonObject.get(key), searchQuery)
                    }
                }
            }
        }
    }
}

@Composable
fun JsonArrayViewer(jsonArray: JSONArray, rootName: String, searchQuery: String = "") {
    val matchingIndices = mutableListOf<Int>()
    if (searchQuery.isBlank()) {
        for (i in 0 until jsonArray.length()) matchingIndices.add(i)
    } else {
        for (i in 0 until jsonArray.length()) {
            if (jsonContains(jsonArray.get(i), searchQuery)) {
                matchingIndices.add(i)
            }
        }
    }

    if (matchingIndices.isEmpty() && searchQuery.isNotBlank()) return

    var expanded by remember { mutableStateOf(false) }
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank() && matchingIndices.isNotEmpty()) expanded = true
    }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            HighlightedText(
                text = rootName,
                query = searchQuery,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                " [${matchingIndices.size}/${jsonArray.length()}]",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(start = 16.dp)) {
                matchingIndices.forEach { i ->
                    val value = jsonArray.get(i)
                    JsonNodeViewer("[$i]", value, searchQuery)
                }
            }
        }
    }
}

@Composable
fun JsonNodeViewer(key: String, value: Any, searchQuery: String = "") {
    when (value) {
        is JSONObject -> JsonObjectViewer(value, rootName = key, searchQuery = searchQuery)
        is JSONArray -> JsonArrayViewer(value, rootName = key, searchQuery = searchQuery)
        else -> {
            val keyMatches = key.contains(searchQuery, ignoreCase = true)
            val valueMatches = value.toString().contains(searchQuery, ignoreCase = true)
            if (searchQuery.isBlank() || keyMatches || valueMatches) {
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    HighlightedText(
                        text = "$key: ",
                        query = searchQuery,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    HighlightedText(
                        text = value.toString(),
                        query = searchQuery,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            label,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary
        )
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

@Composable
fun HighlightedText(
    text: String,
    query: String,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    color: Color = Color.Unspecified
) {
    if (query.isBlank()) {
        Text(
            text = text,
            modifier = modifier,
            fontWeight = fontWeight,
            fontSize = fontSize,
            color = color
        )
        return
    }

    val startIndex = text.indexOf(query, ignoreCase = true)
    if (startIndex == -1) {
        Text(
            text = text,
            modifier = modifier,
            fontWeight = fontWeight,
            fontSize = fontSize,
            color = color
        )
        return
    }

    val annotatedString = buildAnnotatedString {
        var currentIndex = 0
        var matchIndex = text.indexOf(query, startIndex = currentIndex, ignoreCase = true)
        while (matchIndex >= 0) {
            append(text.substring(currentIndex, matchIndex))
            withStyle(style = SpanStyle(background = Color.Yellow)) {
                append(text.substring(matchIndex, matchIndex + query.length))
            }
            currentIndex = matchIndex + query.length
            matchIndex = text.indexOf(query, startIndex = currentIndex, ignoreCase = true)
        }
        append(text.substring(currentIndex))
    }

    Text(
        text = annotatedString,
        modifier = modifier,
        fontWeight = fontWeight,
        fontSize = fontSize,
        color = color
    )
}

fun jsonContains(value: Any, query: String): Boolean {
    if (query.isBlank()) return true
    when (value) {
        is JSONObject -> {
            value.keys().forEach { key ->
                if (key.contains(query, ignoreCase = true)) return true
                if (jsonContains(value.get(key), query)) return true
            }
            return false
        }

        is JSONArray -> {
            for (i in 0 until value.length()) {
                if (jsonContains(value.get(i), query)) return true
            }
            return false
        }

        else -> {
            return value.toString().contains(query, ignoreCase = true)
        }
    }
}
