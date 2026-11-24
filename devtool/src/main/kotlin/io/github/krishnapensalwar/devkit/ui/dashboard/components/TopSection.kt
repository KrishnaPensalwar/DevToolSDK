package io.github.krishnapensalwar.devkit.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.krishnapensalwar.devkit.ui.components.DevToolSearchBar
import io.github.krishnapensalwar.devkit.ui.dashboard.network.methodColor
import io.github.krishnapensalwar.devkit.ui.theme.colorStatusError
import io.github.krishnapensalwar.devkit.ui.theme.colorStatusSuccess
import io.github.krishnapensalwar.devkit.ui.theme.colorStatusWarning
import io.github.krishnapensalwar.devkit.ui.theme.sdkBackground
import io.github.krishnapensalwar.devkit.ui.theme.sdkOnSurface
import io.github.krishnapensalwar.devkit.ui.theme.sdkOnSurfaceVariant
import io.github.krishnapensalwar.devkit.ui.theme.sdkPrimary
import io.github.krishnapensalwar.devkit.ui.theme.sdkSurface
import io.github.krishnapensalwar.devkit.ui.theme.sdkSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedMethods: Set<String>,
    onMethodsChange: (Set<String>) -> Unit,
    existingMethods: List<String>,
    selectedStatuses: Set<String>,
    onStatusesChange: (Set<String>) -> Unit,
    selectedTypes: Set<String>,
    onTypesChange: (Set<String>) -> Unit,
    onClearConfirmed: () -> Unit,
) {
    var showClearDialog by rememberSaveable { mutableStateOf(false) }
    var methodMenuOpen by rememberSaveable { mutableStateOf(false) }
    var statusMenuOpen by rememberSaveable { mutableStateOf(false) }
    var typeMenuOpen by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DevToolSearchBar(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = "Search name, endpoint, body...",
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = { showClearDialog = true },
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

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text("Clear Network Logs", fontWeight = FontWeight.Bold, color = sdkOnSurface)
            },
            text = {
                Text(
                    "Are you sure you want to clear all network activity logs? This action cannot be undone.",
                    color = sdkOnSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearConfirmed()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear", color = colorStatusError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = sdkOnSurfaceVariant)
                }
            },
            containerColor = sdkSurface,
            textContentColor = sdkOnSurfaceVariant,
            titleContentColor = sdkOnSurface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterMenuChip(
                label = if (selectedMethods.isEmpty()) "Method: All" else "Method: ${selectedMethods.joinToString(", ")}",
                selected = selectedMethods.isNotEmpty(),
                expanded = methodMenuOpen,
                onExpandedChange = { methodMenuOpen = it },
                options = existingMethods,
                selectedOptions = selectedMethods,
                onSelectedChange = onMethodsChange,
                optionColor = { methodColor(it) }
            )
        }
        item {
            FilterMenuChip(
                label = if (selectedStatuses.isEmpty()) "Status: All" else "Status: ${selectedStatuses.joinToString(", ")}",
                selected = selectedStatuses.isNotEmpty(),
                expanded = statusMenuOpen,
                onExpandedChange = { statusMenuOpen = it },
                options = listOf("Success", "Failed", "Pending"),
                selectedOptions = selectedStatuses,
                onSelectedChange = onStatusesChange,
                optionColor = { status ->
                    when (status) {
                        "Success" -> colorStatusSuccess
                        "Failed" -> colorStatusError
                        "Pending" -> colorStatusWarning
                        else -> sdkOnSurface
                    }
                }
            )
        }
        item {
            FilterMenuChip(
                label = if (selectedTypes.isEmpty()) "Type: All" else "Type: ${selectedTypes.joinToString(", ")}",
                selected = selectedTypes.isNotEmpty(),
                expanded = typeMenuOpen,
                onExpandedChange = { typeMenuOpen = it },
                options = listOf("REST", "GraphQL"),
                selectedOptions = selectedTypes,
                onSelectedChange = onTypesChange,
                optionColor = { sdkPrimary }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterMenuChip(
    label: String,
    selected: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<String>,
    selectedOptions: Set<String>,
    onSelectedChange: (Set<String>) -> Unit,
    optionColor: (String) -> Color,
) {
    Box {
        FilterChip(
            selected = selected,
            onClick = { onExpandedChange(true) },
            label = { Text(label, fontSize = 14.sp) },
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
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(sdkSurface)
        ) {
            options.forEach { option ->
                val isChecked = selectedOptions.contains(option)
                val color = optionColor(option)
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    onSelectedChange(
                                        if (checked) selectedOptions + option else selectedOptions - option
                                    )
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = color,
                                    checkmarkColor = sdkBackground
                                ),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(option, color = color, fontWeight = FontWeight.Bold)
                        }
                    },
                    onClick = {
                        onSelectedChange(
                            if (isChecked) selectedOptions - option else selectedOptions + option
                        )
                    }
                )
            }
        }
    }
}
