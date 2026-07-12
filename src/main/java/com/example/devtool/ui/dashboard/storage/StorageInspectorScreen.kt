package com.example.devtool.ui.dashboard.storage

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

@Composable
fun StorageInspectorScreen(modifier: Modifier = Modifier) {
    var selectedView by remember { mutableStateOf<StorageView>(StorageView.Home) }

    Column(modifier = modifier) {
        when (val view = selectedView) {
            is StorageView.Home -> StorageHome(onSelect = { selectedView = it })
            is StorageView.SharedPreferences -> SharedPreferencesList(onBack = { selectedView = StorageView.Home })
            is StorageView.Databases -> DatabaseList(onBack = { selectedView = StorageView.Home })
        }
    }
}

sealed class StorageView {
    object Home : StorageView()
    object SharedPreferences : StorageView()
    object Databases : StorageView()
}

@Composable
fun StorageHome(onSelect: (StorageView) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        StorageCategoryCard(
            title = "SharedPreferences",
            icon = Icons.Default.Folder,
            onClick = { onSelect(StorageView.SharedPreferences) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        StorageCategoryCard(
            title = "Databases (Room/SQLite)",
            icon = Icons.Default.Storage,
            onClick = { onSelect(StorageView.Databases) }
        )
    }
}

@Composable
fun StorageCategoryCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun SharedPreferencesList(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
    val prefFiles = prefsDir.listFiles()?.map { it.name }?.filter { 
        it.contains("auth", ignoreCase = true)
    } ?: emptyList()

    Column(modifier = Modifier.fillMaxSize()) {
        TextButton(onClick = onBack, modifier = Modifier.padding(16.dp)) {
            Text("< Back to Storage")
        }
        
        if (prefFiles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No Auth SharedPreferences found")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                items(prefFiles) { fileName ->
                    var expanded by remember { mutableStateOf(false) }
                    val prefName = fileName.removeSuffix(".xml")
                    val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                    val allEntries = prefs.all

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expanded = !expanded }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(fileName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                Icon(
                                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Expand"
                                )
                            }
                            
                            AnimatedVisibility(visible = expanded) {
                                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                                    if (allEntries.isEmpty()) {
                                        Text("Empty", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    } else {
                                        allEntries.forEach { (key, value) ->
                                            Column {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 8.dp),
                                                    verticalAlignment = Alignment.Top
                                                ) {
                                                    Text(
                                                        text = key,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Text(
                                                        text = value.toString(),
                                                        fontSize = 14.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.weight(1f),
                                                        textAlign = TextAlign.End
                                                    )
                                                }
                                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DatabaseList(onBack: () -> Unit) {
    val context = LocalContext.current
    val dbFiles = context.databaseList().filter { !it.endsWith("-journal") && !it.endsWith("-shm") && !it.endsWith("-wal") }

    Column(modifier = Modifier.fillMaxSize()) {
        TextButton(onClick = onBack, modifier = Modifier.padding(8.dp)) {
            Text("< Back to Storage")
        }

        if (dbFiles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No Databases found")
            }
        } else {
            LazyColumn {
                items(dbFiles) { dbName ->
                    ListItem(
                        headlineContent = { Text(dbName) },
                        modifier = Modifier.clickable { /* Detail view could be added */ }
                    )
                }
            }
        }
    }
}
