package com.example.devtool.ui.dashboard.storage

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devtool.ui.components.*
import com.example.devtool.ui.theme.*
import java.io.File

@Composable
fun StorageInspectorScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Databases", "SharedPrefs")

    Column(modifier = modifier.background(sdkBackground)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                FilterChip(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    label = { Text(title) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = sdkSurfaceVariant,
                        selectedLabelColor = sdkPrimary,
                        containerColor = sdkSurface,
                        labelColor = sdkOnSurfaceVariant
                    ),
                    border = null,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

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

    if (dbFiles.isEmpty()) {
        EmptyStateView(message = "No databases found")
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(dbFiles) { dbName ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = sdkSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = sdkOnSurfaceVariant)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(dbName, color = sdkOnSurface, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun SharedPreferencesSection() {
    val context = LocalContext.current
    val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
    val allPrefFiles = prefsDir.listFiles()?.map { it.name } ?: emptyList()

    if (allPrefFiles.isEmpty()) {
        EmptyStateView(message = "No SharedPreferences found")
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(allPrefFiles) { fileName ->
                SharedPrefsCard(fileName = fileName, context = context)
            }
        }
    }
}

@Composable
fun SharedPrefsCard(fileName: String, context: Context) {
    var expanded by remember { mutableStateOf(false) }
    val prefName = fileName.removeSuffix(".xml")
    val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    val entries = prefs.all

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = sdkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = sdkOnSurfaceVariant)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(prefName, color = sdkOnSurface, fontWeight = FontWeight.Bold)
                    Text("${entries.size} items", color = sdkOnSurfaceVariant, fontSize = 11.sp)
                }
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = sdkOnSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    entries.forEach { (key, value) ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(key, color = sdkPrimary, fontSize = 12.sp, modifier = Modifier.weight(0.4f), fontWeight = FontWeight.Bold)
                            Text(value.toString(), color = sdkOnSurface, fontSize = 12.sp, modifier = Modifier.weight(0.6f))
                        }
                        HorizontalDivider(color = sdkSurfaceVariant, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}
