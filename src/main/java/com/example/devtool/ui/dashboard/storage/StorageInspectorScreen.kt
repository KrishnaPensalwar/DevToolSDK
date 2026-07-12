package com.example.devtool.ui.dashboard.storage

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devtool.ui.components.*
import com.example.devtool.ui.theme.surfaceContainerHigh
import java.io.File

@Composable
fun StorageInspectorScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Databases", "SharedPreferences")

    Column(modifier = modifier.background(MaterialTheme.colorScheme.background)) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                DevToolTabChip(
                    label = title,
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    leadingIcon = if (index == 0) {
                        {
                            Icon(
                                Icons.Default.Storage,
                                contentDescription = null,
                                tint = if (selectedTab == index) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = null,
                                tint = if (selectedTab == index) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)

        when (selectedTab) {
            0 -> DatabasesSection()
            1 -> SharedPreferencesSection()
        }
    }
}

@Composable
fun DatabasesSection() {
    val context = LocalContext.current
    val dbFiles = context.databaseList()
        .filter { !it.endsWith("-journal") && !it.endsWith("-shm") && !it.endsWith("-wal") }

    var searchQuery by remember { mutableStateOf("") }
    val filtered = if (searchQuery.isBlank()) dbFiles
    else dbFiles.filter { it.contains(searchQuery, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize()) {
        DevToolSearchBar(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = "Filter tables...",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )

        if (filtered.isEmpty()) {
            EmptyStateView(message = "No databases found")
        } else {
            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tables",
                        style = MaterialTheme.typography.headlineMedium,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SQLITE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                filtered.forEachIndexed { index, dbName ->
                    DatabaseTableItem(
                        name = dbName,
                        selected = index == 0
                    )
                }
            }
        }
    }
}

@Composable
fun DatabaseTableItem(name: String, selected: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                else androidx.compose.ui.graphics.Color.Transparent
            )
            .then(
                if (selected) Modifier.border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                ) else Modifier
            )
            .clickable { /* open table detail */ }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Default.TableChart,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SharedPreferencesSection() {
    val context = LocalContext.current
    val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
    val allPrefFiles = prefsDir.listFiles()?.map { it.name }?.filter {
        it.contains("auth", ignoreCase = true)
    } ?: emptyList()

    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        DevToolSearchBar(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = "Filter keys/values...",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )

        if (allPrefFiles.isEmpty()) {
            EmptyStateView(message = "No Auth SharedPreferences found")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allPrefFiles) { fileName ->
                    SharedPrefsCard(fileName = fileName, context = context, searchQuery = searchQuery)
                }
            }
        }
    }
}

@Composable
fun SharedPrefsCard(fileName: String, context: Context, searchQuery: String) {
    var expanded by remember { mutableStateOf(true) }
    val prefName = fileName.removeSuffix(".xml")
    val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    val allEntries = prefs.all

    val filteredEntries = if (searchQuery.isBlank()) allEntries
    else allEntries.filter {
        it.key.contains(searchQuery, ignoreCase = true) ||
                it.value.toString().contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = prefName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${filteredEntries.size} keys",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surfaceContainerHigh.copy(alpha = 0.3f))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (filteredEntries.isEmpty()) {
                    Text(
                        text = if (searchQuery.isBlank()) "Empty" else "No matching keys",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "KEY",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "VALUE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End
                        )
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        thickness = 0.5.dp
                    )
                    filteredEntries.forEach { (key, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = key,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = value.toString(),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End,
                                maxLines = 3
                            )
                        }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}
