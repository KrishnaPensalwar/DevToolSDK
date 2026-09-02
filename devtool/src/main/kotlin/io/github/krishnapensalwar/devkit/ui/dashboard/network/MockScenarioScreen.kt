package io.github.krishnapensalwar.devkit.ui.dashboard.network

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import io.github.krishnapensalwar.devkit.cache.CacheManager
import io.github.krishnapensalwar.devkit.mock.scenario.CustomScenarioData
import io.github.krishnapensalwar.devkit.mock.scenario.MockApiUiState
import io.github.krishnapensalwar.devkit.mock.scenario.MockScenarioCatalog
import io.github.krishnapensalwar.devkit.mock.scenario.MockScenarioGroup
import io.github.krishnapensalwar.devkit.mock.scenario.MockScenarioRepository
import io.github.krishnapensalwar.devkit.mock.scenario.MockScenarioType
import io.github.krishnapensalwar.devkit.ui.navigation.pop
import io.github.krishnapensalwar.devkit.ui.theme.colorStatusError
import io.github.krishnapensalwar.devkit.ui.theme.colorStatusSuccess
import io.github.krishnapensalwar.devkit.ui.theme.colorStatusWarning
import io.github.krishnapensalwar.devkit.ui.theme.sdkBackground
import io.github.krishnapensalwar.devkit.ui.theme.sdkOnSurface
import io.github.krishnapensalwar.devkit.ui.theme.sdkOnSurfaceVariant
import io.github.krishnapensalwar.devkit.ui.theme.sdkSurface
import io.github.krishnapensalwar.devkit.ui.theme.sdkSurfaceVariant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun MockScenarioScreen(
    url: String,
    method: String,
    capturedBody: String,
    capturedStatus: Int,
    capturedHeadersJson: String,
    requestBody: String,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val identity = remember(url, method, requestBody) {
        io.github.krishnapensalwar.devkit.mock.identity.RequestIdentity.parse(url, method, requestBody)
    }
    val state by MockScenarioRepository.observe(identity).collectAsState(initial = MockApiUiState())

    var customEditor by remember { mutableStateOf<CustomScenarioData?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var customDelayText by remember { mutableStateOf("") }

    LaunchedEffect(identity.identityKey, capturedBody) {
        withContext(Dispatchers.IO) {
            if (capturedBody.isNotBlank()) {
                MockScenarioRepository.saveSnapshot(
                    identity = identity,
                    status = if (capturedStatus in 100..599) capturedStatus else 200,
                    headersJson = capturedHeadersJson.ifBlank { "{}" },
                    body = capturedBody
                )
                if (!identity.isGraphQl) {
                    CacheManager.saveWithHeadersJson(
                        url = url,
                        method = method,
                        status = if (capturedStatus in 100..599) capturedStatus else 200,
                        headersJson = capturedHeadersJson.ifBlank { "{}" },
                        body = capturedBody
                    )
                }
            }
        }
    }

    val activeBuiltIn = MockScenarioCatalog.findByKey(state.activeScenarioKey.orEmpty())
    val activeCustom = state.customScenarios.find { it.key == state.activeScenarioKey }

    Scaffold(
        modifier = modifier,
        containerColor = sdkBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.pop() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(sdkSurface)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = sdkOnSurface)
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = if (identity.isGraphQl) "GraphQL Scenarios" else "Mock Scenarios",
                        fontWeight = FontWeight.Bold,
                        color = sdkOnSurface
                    )
                    Text(
                        text = if (identity.isGraphQl) {
                            "${identity.graphQlOperationType}  ${identity.displayName}"
                        } else {
                            "$method  $url"
                        },
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = sdkOnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = { showCreate = true },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(sdkSurface)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add custom", tint = sdkOnSurface)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = sdkSurface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF1E3A2F)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Science, contentDescription = null, tint = colorStatusSuccess, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("API Mock", fontWeight = FontWeight.Bold, color = sdkOnSurface)
                                Text(
                                    if (state.enabled) "This endpoint uses the active scenario" else "This endpoint hits the live server",
                                    fontSize = 12.sp,
                                    color = sdkOnSurfaceVariant
                                )
                            }
                            Switch(
                                checked = state.enabled,
                                onCheckedChange = { enabled ->
                                    scope.launch(Dispatchers.IO) {
                                        MockScenarioRepository.setApiEnabled(identity, enabled)
                                    }
                                }
                            )
                        }

                        HorizontalDivider(color = sdkSurfaceVariant)
                        val scenarioLabel = activeBuiltIn?.title ?: activeCustom?.name ?: "None"
                        val statusLabel = when {
                            activeBuiltIn?.statusCode != null -> activeBuiltIn.statusCode.toString()
                            activeCustom != null -> activeCustom.statusCode.toString()
                            else -> "—"
                        }
                        Text("MOCKED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colorStatusSuccess)
                        Text("Scenario: $scenarioLabel", color = sdkOnSurface, fontWeight = FontWeight.SemiBold)
                        Text("Status: $statusLabel", fontSize = 13.sp, color = sdkOnSurfaceVariant)
                    }
                }
            }

            MockScenarioCatalog.groupedFor(identity.isGraphQl).forEach { (group, scenarios) ->
                item {
                    ScenarioGroupHeader(MockScenarioCatalog.groupLabel(group))
                }
                items(scenarios, key = { it.key }) { scenario ->
                    val selected = state.activeScenarioKey == scenario.key
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScenarioRow(
                            title = scenario.title,
                            subtitle = scenario.subtitle,
                            badge = scenario.statusCode?.toString(),
                            selected = selected,
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    MockScenarioRepository.activateScenario(identity, scenario.key)
                                }
                            }
                        )
                        if (scenario.type == MockScenarioType.SLOW_RESPONSE && selected) {
                            SlowDelayPicker(
                                selectedMs = state.slowDelayMs,
                                customText = customDelayText,
                                onCustomTextChange = { customDelayText = it.filter { ch -> ch.isDigit() }.take(6) },
                                onSelect = { delay ->
                                    scope.launch(Dispatchers.IO) {
                                        MockScenarioRepository.setSlowDelay(identity, delay)
                                    }
                                },
                                onApplyCustom = {
                                    val value = customDelayText.toLongOrNull()
                                    if (value != null) {
                                        scope.launch(Dispatchers.IO) {
                                            MockScenarioRepository.setSlowDelay(identity, value)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            item { ScenarioGroupHeader(MockScenarioCatalog.groupLabel(MockScenarioGroup.CUSTOM)) }

            if (state.customScenarios.isEmpty()) {
                item {
                    Text(
                        "No custom scenarios. Tap + to add one.",
                        color = sdkOnSurfaceVariant,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            items(state.customScenarios, key = { it.id }) { custom ->
                val selected = state.activeScenarioKey == custom.key
                CustomScenarioRow(
                    custom = custom,
                    selected = selected,
                    onSelect = {
                        scope.launch(Dispatchers.IO) {
                            MockScenarioRepository.activateScenario(identity, custom.key)
                        }
                    },
                    onEdit = { customEditor = custom },
                    onDelete = {
                        scope.launch(Dispatchers.IO) {
                            MockScenarioRepository.deleteCustom(custom.id, identity)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Deleted ${custom.name}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showCreate) {
        CustomScenarioEditorDialog(
            initial = CustomScenarioData(
                id = 0,
                name = "",
                description = "",
                statusCode = if (identity.isGraphQl) 200 else 202,
                body = capturedBody.ifBlank {
                    if (identity.isGraphQl) """{"data":{"status":"pending"}}"""
                    else """{"status":"pending"}"""
                },
                headers = mapOf("Content-Type" to "application/json"),
                delayMs = 0
            ),
            onDismiss = { showCreate = false },
            onSave = { data ->
                scope.launch(Dispatchers.IO) {
                    val id = MockScenarioRepository.insertCustom(
                        identity = identity,
                        name = data.name,
                        description = data.description,
                        statusCode = data.statusCode,
                        body = data.body,
                        headers = data.headers,
                        delayMs = data.delayMs
                    )
                    if (id > 0) {
                        MockScenarioRepository.activateScenario(identity, "custom:$id")
                    }
                    withContext(Dispatchers.Main) { showCreate = false }
                }
            }
        )
    }

    customEditor?.let { editing ->
        CustomScenarioEditorDialog(
            initial = editing,
            onDismiss = { customEditor = null },
            onSave = { data ->
                scope.launch(Dispatchers.IO) {
                    MockScenarioRepository.updateCustom(data.copy(id = editing.id), identity)
                    withContext(Dispatchers.Main) { customEditor = null }
                }
            }
        )
    }
}

@Composable
private fun ScenarioGroupHeader(label: String) {
    Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        color = sdkOnSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, start = 4.dp)
    )
}

@Composable
private fun ScenarioRow(
    title: String,
    subtitle: String,
    badge: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    val border by animateColorAsState(
        if (selected) colorStatusSuccess else Color.Transparent,
        label = "scenario-border"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFF13261C) else sdkSurface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = sdkOnSurface)
                Text(subtitle, fontSize = 12.sp, color = sdkOnSurfaceVariant)
            }
            if (badge != null) {
                StatusBadge(badge)
                Spacer(Modifier.width(8.dp))
            }
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = colorStatusSuccess, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun CustomScenarioRow(
    custom: CustomScenarioData,
    selected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val border by animateColorAsState(
        if (selected) colorStatusSuccess else Color.Transparent,
        label = "custom-border"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, border, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFF13261C) else sdkSurface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onSelect)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(custom.name.ifBlank { "Custom" }, fontWeight = FontWeight.SemiBold, color = sdkOnSurface)
                if (custom.description.isNotBlank()) {
                    Text(custom.description, fontSize = 12.sp, color = sdkOnSurfaceVariant, maxLines = 2)
                }
                Text(
                    "Status ${custom.statusCode} · ${custom.delayMs}ms",
                    fontSize = 11.sp,
                    color = sdkOnSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = sdkOnSurfaceVariant, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = colorStatusError, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun StatusBadge(code: String) {
    val color = when (code.toIntOrNull()) {
        in 200..299 -> colorStatusSuccess
        in 400..499 -> colorStatusWarning
        in 500..599 -> colorStatusError
        else -> sdkOnSurfaceVariant
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(code, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun SlowDelayPicker(
    selectedMs: Long,
    customText: String,
    onCustomTextChange: (String) -> Unit,
    onSelect: (Long) -> Unit,
    onApplyCustom: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(sdkSurfaceVariant)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Delay", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = sdkOnSurfaceVariant)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(MockScenarioCatalog.slowDelayPresetsMs) { delay ->
                val selected = selectedMs == delay
                FilterChip(
                    selected = selected,
                    onClick = { onSelect(delay) },
                    label = { Text(formatDelay(delay), fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colorStatusSuccess.copy(alpha = 0.2f),
                        selectedLabelColor = colorStatusSuccess,
                        containerColor = sdkSurface,
                        labelColor = sdkOnSurface
                    )
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = customText,
                onValueChange = onCustomTextChange,
                label = { Text("Custom ms") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorStatusSuccess,
                    unfocusedBorderColor = sdkOnSurfaceVariant
                )
            )
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = onApplyCustom) { Text("Apply") }
        }
    }
}

private fun formatDelay(ms: Long): String = when {
    ms < 1000 -> "${ms}ms"
    ms % 1000L == 0L -> "${ms / 1000}s"
    else -> "${ms}ms"
}

@Composable
private fun CustomScenarioEditorDialog(
    initial: CustomScenarioData,
    onDismiss: () -> Unit,
    onSave: (CustomScenarioData) -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var description by remember { mutableStateOf(initial.description) }
    var status by remember { mutableStateOf(initial.statusCode.toString()) }
    var delay by remember { mutableStateOf(initial.delayMs.toString()) }
    var body by remember { mutableStateOf(initial.body) }
    var headers by remember {
        mutableStateOf(initial.headers.entries.joinToString("\n") { "${it.key}: ${it.value}" })
    }
    val statusValue = status.toIntOrNull()
    val canSave = name.isNotBlank() && statusValue != null && statusValue in 100..599

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = sdkSurface,
        title = { Text(if (initial.id == 0L) "New custom scenario" else "Edit custom scenario", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = { status = it.filter { ch -> ch.isDigit() }.take(3) },
                        label = { Text("Status") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = delay,
                        onValueChange = { delay = it.filter { ch -> ch.isDigit() }.take(6) },
                        label = { Text("Delay ms") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = headers,
                    onValueChange = { headers = it },
                    label = { Text("Headers (Name: Value)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Body") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                )
            }
        },
        confirmButton = {
            Button(
                enabled = canSave,
                onClick = {
                    onSave(
                        initial.copy(
                            name = name.trim(),
                            description = description.trim(),
                            statusCode = statusValue ?: 200,
                            delayMs = delay.toLongOrNull() ?: 0L,
                            body = body,
                            headers = parseHeaderLines(headers)
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = colorStatusSuccess, contentColor = Color.Black)
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = sdkOnSurfaceVariant) }
        }
    )
}

private fun parseHeaderLines(raw: String): Map<String, String> {
    if (raw.isBlank()) return mapOf("Content-Type" to "application/json")
    return raw.lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() && it.contains(":") }
        .associate { line ->
            val idx = line.indexOf(':')
            line.substring(0, idx).trim() to line.substring(idx + 1).trim()
        }
        .ifEmpty { mapOf("Content-Type" to "application/json") }
}
