package io.github.krishnapensalwar.devkit.ui.dashboard.network

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import io.github.krishnapensalwar.devkit.core.logging.LoggerManager
import io.github.krishnapensalwar.devkit.core.utils.ApiNameExtractor
import io.github.krishnapensalwar.devkit.network.model.NetworkCall
import io.github.krishnapensalwar.devkit.ui.components.DevToolSearchBar
import io.github.krishnapensalwar.devkit.ui.components.StatusDot
import io.github.krishnapensalwar.devkit.ui.dashboard.network.components.NetworkSummaryBadge
import io.github.krishnapensalwar.devkit.ui.navigation.Destination
import io.github.krishnapensalwar.devkit.ui.navigation.navigateTo
import io.github.krishnapensalwar.devkit.ui.theme.colorStatusError
import io.github.krishnapensalwar.devkit.ui.theme.colorStatusNeutral
import io.github.krishnapensalwar.devkit.ui.theme.colorStatusSuccess
import io.github.krishnapensalwar.devkit.ui.theme.colorStatusWarning
import io.github.krishnapensalwar.devkit.ui.theme.methodDelete
import io.github.krishnapensalwar.devkit.ui.theme.methodGet
import io.github.krishnapensalwar.devkit.ui.theme.methodPatch
import io.github.krishnapensalwar.devkit.ui.theme.methodPost
import io.github.krishnapensalwar.devkit.ui.theme.methodPut
import io.github.krishnapensalwar.devkit.ui.theme.sdkBackground
import io.github.krishnapensalwar.devkit.ui.theme.sdkOnSurface
import io.github.krishnapensalwar.devkit.ui.theme.sdkOnSurfaceVariant
import io.github.krishnapensalwar.devkit.ui.theme.sdkPrimary
import io.github.krishnapensalwar.devkit.ui.theme.sdkSurface
import io.github.krishnapensalwar.devkit.ui.theme.sdkSurfaceVariant
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NetworkListScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val repository = LoggerManager.getNetworkRepository()
    val calls by repository.calls.collectAsState()
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedMethods by remember { mutableStateOf(setOf<String>()) }
    var selectedStatuses by remember { mutableStateOf(setOf<String>()) }

    var isMethodDropdownExpanded by remember { mutableStateOf(false) }
    var isStatusDropdownExpanded by remember { mutableStateOf(false) }
    var showClearNetworkDialog by remember { mutableStateOf(false) }

    val existingMethods = remember(calls) {
        val methods = calls.map { it.method.uppercase() }.distinct()
        if (methods.isEmpty()) listOf("GET", "POST", "PUT", "DELETE", "PATCH") else methods.sorted()
    }

    val filteredCalls = calls.filter { call ->
        val matchesQuery = searchQuery.isEmpty() ||
                call.url.contains(searchQuery, ignoreCase = true) ||
                call.endpoint.contains(searchQuery, ignoreCase = true)
        val matchesMethod = selectedMethods.isEmpty() ||
                selectedMethods.contains(call.method.uppercase())
        val matchesStatus = selectedStatuses.isEmpty() || selectedStatuses.any { status ->
            when (status) {
                "Success" -> call.statusCode in 200..299
                "Failed" -> call.statusCode in 400..599 || call.exception != null
                "Pending" -> call.statusCode <= 0 && call.exception == null
                else -> true
            }
        }
        matchesQuery && matchesMethod && matchesStatus
    }

    Column(modifier = modifier.background(sdkBackground)) {

//        NetworkSummaryBadge(calls = calls)

        // Sticky filter header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(sdkBackground)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DevToolSearchBar(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Filter traffic...",
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { showClearNetworkDialog = true },
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(sdkSurface)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Clear All",
                        tint = sdkOnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (showClearNetworkDialog) {
                AlertDialog(
                    onDismissRequest = { showClearNetworkDialog = false },
                    title = {
                        Text(
                            text = "Clear Network Logs",
                            fontWeight = FontWeight.Bold,
                            color = sdkOnSurface
                        )
                    },
                    text = {
                        Text(
                            text = "Are you sure you want to clear all network activity logs? This action cannot be undone.",
                            color = sdkOnSurfaceVariant
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    repository.clearAll()
                                }
                                showClearNetworkDialog = false
                            }
                        ) {
                            Text("Clear", color = colorStatusError)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showClearNetworkDialog = false }
                        ) {
                            Text("Cancel", color = sdkOnSurfaceVariant)
                        }
                    },
                    containerColor = sdkSurface,
                    textContentColor = sdkOnSurfaceVariant,
                    titleContentColor = sdkOnSurface,
                    shape = RoundedCornerShape(24.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Method dropdown chip
                Box {
                    val hasMethodFilter = selectedMethods.isNotEmpty()
                    FilterChip(
                        selected = hasMethodFilter,
                        onClick = { isMethodDropdownExpanded = true },
                        label = {
                            Text(
                                text = if (hasMethodFilter) "Method: ${selectedMethods.joinToString(", ")}" else "Method: All",
                                fontSize = 14.sp
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = sdkSurfaceVariant,
                            selectedLabelColor = sdkPrimary,
                            containerColor = sdkSurface,
                            labelColor = sdkOnSurfaceVariant
                        ),
                        border = null,
                        shape = RoundedCornerShape(10.dp)
                    )

                    DropdownMenu(
                        expanded = isMethodDropdownExpanded,
                        onDismissRequest = { isMethodDropdownExpanded = false },
                        modifier = Modifier.background(sdkSurface)
                    ) {
                        existingMethods.forEach { method ->
                            val isChecked = selectedMethods.contains(method)
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        val targetColor = methodColor(method)
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                selectedMethods = if (checked) {
                                                    selectedMethods + method
                                                } else {
                                                    selectedMethods - method
                                                }
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = targetColor,
                                                checkmarkColor = sdkBackground
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = method, color = targetColor, fontWeight = FontWeight.Bold)
                                    }
                                },
                                onClick = {
                                    selectedMethods = if (isChecked) {
                                        selectedMethods - method
                                    } else {
                                        selectedMethods + method
                                    }
                                }
                            )
                        }
                    }
                }

                // Status dropdown chip
                Box {
                    val hasStatusFilter = selectedStatuses.isNotEmpty()
                    FilterChip(
                        selected = hasStatusFilter,
                        onClick = { isStatusDropdownExpanded = true },
                        label = {
                            Text(
                                text = if (hasStatusFilter) "Status: ${selectedStatuses.joinToString(", ")}" else "Status: All",
                                fontSize = 14.sp
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = sdkSurfaceVariant,
                            selectedLabelColor = sdkPrimary,
                            containerColor = sdkSurface,
                            labelColor = sdkOnSurfaceVariant
                        ),
                        border = null,
                        shape = RoundedCornerShape(10.dp)
                    )

                    DropdownMenu(
                        expanded = isStatusDropdownExpanded,
                        onDismissRequest = { isStatusDropdownExpanded = false },
                        modifier = Modifier.background(sdkSurface)
                    ) {
                        listOf("Success", "Failed", "Pending").forEach { status ->
                            val isChecked = selectedStatuses.contains(status)
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        val targetColor = when (status) {
                                            "Success" -> colorStatusSuccess
                                            "Failed" -> colorStatusError
                                            "Pending" -> colorStatusWarning
                                            else -> sdkOnSurface
                                        }
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                selectedStatuses = if (checked) {
                                                    selectedStatuses + status
                                                } else {
                                                    selectedStatuses - status
                                                }
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = targetColor,
                                                checkmarkColor = sdkBackground
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = status, color = targetColor, fontWeight = FontWeight.Bold)
                                    }
                                },
                                onClick = {
                                    selectedStatuses = if (isChecked) {
                                        selectedStatuses - status
                                    } else {
                                        selectedStatuses + status
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        if (filteredCalls.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No network activity", color = sdkOnSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredCalls) { call ->
                    NetworkCallItem(
                        call = call,
                        onClick = { navController.navigateTo(Destination.NetworkDetail(call.id)) }
                    )
                }
            }
        }
    }
}

@Composable
internal fun NetworkCallItem(
    call: NetworkCall,
    onClick: () -> Unit
) {
    val statusColor = when {
        call.statusCode in 200..299 -> colorStatusSuccess
        call.statusCode in 300..399 -> colorStatusWarning
        call.statusCode in 400..599 -> colorStatusError
        else -> colorStatusNeutral
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = sdkSurface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = ApiNameExtractor.extract(call.url),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = sdkOnSurface
                )

                Spacer(Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = methodColor(call.method).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = call.method,
                            color = methodColor(call.method),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = call.host,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = sdkOnSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusDot(color = statusColor, modifier = Modifier.size(6.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = call.statusCode.toString(),
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = "${call.duration}ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = sdkOnSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                null,
                tint = sdkOnSurface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

internal fun methodColor(method: String): Color = when (method.uppercase()) {
    "GET"    -> methodGet
    "POST"   -> methodPost
    "PUT"    -> methodPut
    "DELETE" -> methodDelete
    "PATCH"  -> methodPatch
    else     -> colorStatusNeutral
}
