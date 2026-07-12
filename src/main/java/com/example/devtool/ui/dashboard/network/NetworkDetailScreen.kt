package com.example.devtool.ui.dashboard.network

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devtool.network.model.NetworkCall
import com.example.devtool.ui.components.DevToolSearchBar
import com.example.devtool.ui.components.SectionLabel
import com.example.devtool.ui.components.StatusDot
import com.example.devtool.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject

private enum class DetailTab { OVERVIEW, REQUEST, RESPONSE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkDetailScreen(
    call: NetworkCall,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableStateOf(DetailTab.OVERVIEW) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Traffic Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(formatFullLog(call)))
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                // Segmented control
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(4.dp)
                ) {
                    DetailTab.entries.forEach { tab ->
                        val selected = selectedTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.surfaceVariant
                                    else Color.Transparent
                                )
                                .clickable { selectedTab = tab }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontSize = 14.sp,
                                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                                color = if (selected) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            when (selectedTab) {
                DetailTab.OVERVIEW -> OverviewContent(call)
                DetailTab.REQUEST -> RequestContent(call)
                DetailTab.RESPONSE -> ResponseContent(call)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun OverviewContent(call: NetworkCall) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        DetailRow(label = "URL", value = call.url)
        DetailRow(
            label = "Method",
            value = call.method,
            valueContent = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(surfaceContainerHigh)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = call.method,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = methodColor(call.method)
                    )
                }
            }
        )
        DetailRow(
            label = "Status",
            valueContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusDot(
                        color = when {
                            call.statusCode in 200..299 -> colorStatusSuccess
                            call.statusCode >= 400 -> colorStatusError
                            else -> colorStatusNeutral
                        }
                    )
                    Text(
                        text = call.statusCode.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = when {
                            call.statusCode in 200..299 -> colorStatusSuccess
                            call.statusCode >= 400 -> colorStatusError
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        DetailRow(label = "Duration", value = "${call.duration}ms")
        DetailRow(label = "Size", value = "${call.responseSize}B")
        DetailRow(label = "Protocol", value = call.protocol.uppercase())
        DetailRow(label = "Host", value = call.host)
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String? = null,
    valueContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(72.dp)
        )
        if (valueContent != null) {
            Box(modifier = Modifier.weight(1f)) { valueContent() }
        } else {
            Text(
                text = value.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RequestContent(call: NetworkCall) {
    SectionLabel(text = "Request", modifier = Modifier.padding(vertical = 8.dp))
    CollapsibleSection(title = "Request Headers", count = call.requestHeaders.size, defaultExpanded = true) {
        SearchableHeaderList(headers = call.requestHeaders)
    }
    Spacer(modifier = Modifier.height(8.dp))
    CollapsibleSection(title = "Request Body", count = null, defaultExpanded = true) {
        SearchableBody(body = call.requestBody)
    }
}

@Composable
private fun ResponseContent(call: NetworkCall) {
    SectionLabel(text = "Response", modifier = Modifier.padding(vertical = 8.dp))
    CollapsibleSection(title = "Response Headers", count = call.responseHeaders.size, defaultExpanded = false) {
        SearchableHeaderList(headers = call.responseHeaders)
    }
    Spacer(modifier = Modifier.height(8.dp))
    CollapsibleSection(title = "Response Body", count = null, defaultExpanded = true) {
        SearchableBody(body = call.responseBody)
    }
}

@Composable
fun CollapsibleSection(
    title: String,
    count: Int?,
    defaultExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(defaultExpanded) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(0.5.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                .clickable { expanded = !expanded }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (count != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = count.toString(),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                    .background(surfaceContainerLowest)
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
                    .padding(12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SearchableHeaderList(headers: Map<String, String>) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredHeaders = if (searchQuery.isBlank()) headers
    else headers.filter {
        it.key.contains(searchQuery, ignoreCase = true) ||
                it.value.contains(searchQuery, ignoreCase = true)
    }

    Column {
        DevToolSearchBar(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = "Search headers...",
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        if (filteredHeaders.isEmpty()) {
            Text(
                text = "No matching headers",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            filteredHeaders.forEach { (key, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    HighlightedText(
                        text = key,
                        query = searchQuery,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(0.4f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    HighlightedText(
                        text = value,
                        query = searchQuery,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(0.6f)
                    )
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun SearchableBody(body: String?) {
    var searchQuery by remember { mutableStateOf("") }

    Column {
        DevToolSearchBar(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = "Search body...",
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        ExpandableJsonViewer(jsonString = body, searchQuery = searchQuery)
    }
}

@Composable
fun ExpandableJsonViewer(jsonString: String?, searchQuery: String = "") {
    if (jsonString.isNullOrBlank()) {
        Text(
            text = "No body",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val jsonObj = try { JSONObject(jsonString) } catch (e: Exception) { null }
    val jsonArr = try { if (jsonObj == null) JSONArray(jsonString) else null } catch (e: Exception) { null }

    if (jsonObj != null) {
        JsonObjectViewer(jsonObj, rootName = "Root", searchQuery = searchQuery)
    } else if (jsonArr != null) {
        JsonArrayViewer(jsonArr, rootName = "Root", searchQuery = searchQuery)
    } else {
        HighlightedText(
            text = jsonString,
            query = searchQuery,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun JsonObjectViewer(jsonObject: JSONObject, rootName: String? = null, searchQuery: String = "") {
    val keys = jsonObject.keys().asSequence().toList()
    val filteredKeys = if (searchQuery.isBlank()) keys else keys.filter { key ->
        key.contains(searchQuery, ignoreCase = true) || jsonContains(jsonObject.get(key), searchQuery)
    }

    if (filteredKeys.isEmpty() && searchQuery.isNotBlank()) return

    if (rootName == null) {
        Column {
            filteredKeys.forEach { key -> JsonNodeViewer(key, jsonObject.get(key), searchQuery) }
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
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                HighlightedText(
                    text = rootName,
                    query = searchQuery,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    " {${filteredKeys.size}}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AnimatedVisibility(visible = expanded) {
                val color = MaterialTheme.colorScheme.surfaceVariant
                Column(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .drawBehind {
                            drawLine(
                                color = color,
                                start = Offset(0f, 0f),
                                end = Offset(0f, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        .padding(start = 8.dp)
                ) {
                    filteredKeys.forEach { key -> JsonNodeViewer(key, jsonObject.get(key), searchQuery) }
                }
            }
        }
    }
}

@Composable
fun JsonArrayViewer(jsonArray: JSONArray, rootName: String, searchQuery: String = "") {
    val matchingIndices = if (searchQuery.isBlank()) {
        (0 until jsonArray.length()).toList()
    } else {
        (0 until jsonArray.length()).filter { jsonContains(jsonArray.get(it), searchQuery) }
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
                .padding(vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            HighlightedText(
                text = rootName,
                query = searchQuery,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                " [${matchingIndices.size}]",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        AnimatedVisibility(visible = expanded) {
            val color = MaterialTheme.colorScheme.surfaceVariant
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .drawBehind {
                        drawLine(
                            color = color,
                            start = Offset(0f, 0f),
                            end = Offset(0f, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .padding(start = 8.dp)
            ) {
                matchingIndices.forEach { i ->
                    JsonNodeViewer("[$i]", jsonArray.get(i), searchQuery)
                }
            }
        }
    }
}

@Composable
fun JsonNodeViewer(key: String, value: Any, searchQuery: String = "") {
    when (value) {
        is JSONObject -> JsonObjectViewer(value, rootName = key, searchQuery = searchQuery)
        is JSONArray  -> JsonArrayViewer(value, rootName = key, searchQuery = searchQuery)
        else -> {
            val keyMatches = key.contains(searchQuery, ignoreCase = true)
            val valueMatches = value.toString().contains(searchQuery, ignoreCase = true)
            if (searchQuery.isBlank() || keyMatches || valueMatches) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    HighlightedText(
                        text = "$key: ",
                        query = searchQuery,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                    HighlightedText(
                        text = value.toString(),
                        query = searchQuery,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun HighlightedText(
    text: String,
    query: String,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    fontFamily: FontFamily? = null,
    color: Color = Color.Unspecified
) {
    if (query.isBlank()) {
        Text(text = text, modifier = modifier, fontWeight = fontWeight, fontSize = fontSize, fontFamily = fontFamily, color = color)
        return
    }
    val startIndex = text.indexOf(query, ignoreCase = true)
    if (startIndex == -1) {
        Text(text = text, modifier = modifier, fontWeight = fontWeight, fontSize = fontSize, fontFamily = fontFamily, color = color)
        return
    }
    val annotated = buildAnnotatedString {
        var cur = 0
        var match = text.indexOf(query, cur, ignoreCase = true)
        while (match >= 0) {
            append(text.substring(cur, match))
            withStyle(SpanStyle(background = Color(0xFF854D0E), color = Color(0xFFFDE68A))) {
                append(text.substring(match, match + query.length))
            }
            cur = match + query.length
            match = text.indexOf(query, cur, ignoreCase = true)
        }
        append(text.substring(cur))
    }
    Text(text = annotated, modifier = modifier, fontWeight = fontWeight, fontSize = fontSize, fontFamily = fontFamily, color = color)
}

fun jsonContains(value: Any, query: String): Boolean {
    if (query.isBlank()) return true
    return when (value) {
        is JSONObject -> value.keys().asSequence().any { key ->
            key.contains(query, ignoreCase = true) || jsonContains(value.get(key), query)
        }
        is JSONArray -> (0 until value.length()).any { jsonContains(value.get(it), query) }
        else -> value.toString().contains(query, ignoreCase = true)
    }
}

fun formatFullLog(call: NetworkCall): String = buildString {
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
