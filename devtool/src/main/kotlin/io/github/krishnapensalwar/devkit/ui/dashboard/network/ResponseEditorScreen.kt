package io.github.krishnapensalwar.devkit.ui.dashboard.network

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.krishnapensalwar.devkit.ui.components.DevToolSearchBar
import io.github.krishnapensalwar.devkit.ui.dashboard.network.json.ExpandableJsonViewer
import io.github.krishnapensalwar.devkit.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponseEditorScreen(
    initialBody: String,
    endpoint: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var jsonStringState by remember { mutableStateOf(initialBody) }
    var showRawEditor by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Popup Edit Dialog state
    var fieldEditItem by remember { mutableStateOf<Pair<List<String>, Any>?>(null) }
    var editValueText by remember { mutableStateOf("") }

    Scaffold(
        containerColor = sdkBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(sdkBackground)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(sdkSurface)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = sdkOnSurface
                    )
                }

                Text(
                    text = "Response Editor",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = sdkOnSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Toggle Mode Button (Tree vs Raw Text)
                    IconButton(
                        onClick = { showRawEditor = !showRawEditor },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(sdkSurface)
                    ) {
                        Icon(
                            imageVector = if (showRawEditor) Icons.Default.List else Icons.Default.Code,
                            contentDescription = if (showRawEditor) "Switch to Tree Mode" else "Switch to Code Mode",
                            tint = sdkOnSurface
                        )
                    }

                    // Save Button
                    IconButton(
                        onClick = {
                            android.util.Log.d("NetworkInterceptor", "[ResponseEditorScreen] Save button clicked. Propagating updated JSON string to DB save: $jsonStringState")
                            onSave(jsonStringState)
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = "Save",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = endpoint,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = sdkOnSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Search Bar (Always visible at the top)
            DevToolSearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = if (showRawEditor) "Filter code text..." else "Search JSON fields...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            if (showRawEditor) {
                // Code Editor
                OutlinedTextField(
                    value = jsonStringState,
                    onValueChange = { jsonStringState = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = sdkOnSurface
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            } else {
                // Interactive JSON Node Editor
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    ExpandableJsonViewer(
                        jsonString = jsonStringState,
                        searchQuery = searchQuery,
                        onImageClick = {},
                        onFieldClick = { path, value ->
                            fieldEditItem = Pair(path, value)
                            editValueText = value.toString()
                        }
                    )
                }
            }
        }
    }

    // Modal Field Value Editor Dialog
    fieldEditItem?.let { (path, oldValue) ->
        AlertDialog(
            onDismissRequest = { fieldEditItem = null },
            title = { Text("Edit Field Value", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Path: " + path.joinToString(" → "),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editValueText,
                        onValueChange = { editValueText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Value") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsedVal = parseTypedValue(editValueText)
                        android.util.Log.d("NetworkInterceptor", "[ResponseEditorScreen] Apply clicked: path=$path, editValueText='$editValueText', parsedVal=$parsedVal (${parsedVal.javaClass.simpleName})")
                        val updatedJsonStr = try {
                            val rootObj = JSONObject(jsonStringState)
                            updateJsonByPath(rootObj, path, parsedVal)
                            android.util.Log.d("NetworkInterceptor", "[ResponseEditorScreen] Successfully updated JSONObject by path")
                            rootObj.toString(4) // Pretty print JSON
                        } catch (e: Exception) {
                            android.util.Log.d("NetworkInterceptor", "[ResponseEditorScreen] JSONObject update failed: ${e.message}. Trying JSONArray...")
                            try {
                                val rootArr = JSONArray(jsonStringState)
                                updateJsonByPath(rootArr, path, parsedVal)
                                android.util.Log.d("NetworkInterceptor", "[ResponseEditorScreen] Successfully updated JSONArray by path")
                                rootArr.toString(4)
                            } catch (err: Exception) {
                                android.util.Log.e("NetworkInterceptor", "[ResponseEditorScreen] All JSON updates failed: ${err.message}", err)
                                jsonStringState // Fallback if parsing fails
                            }
                        }
                        jsonStringState = updatedJsonStr
                        fieldEditItem = null
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { fieldEditItem = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Helper to determine type parsing automatically
private fun parseTypedValue(input: String): Any {
    if (input.equals("true", ignoreCase = true)) return true
    if (input.equals("false", ignoreCase = true)) return false
    val intValue = input.toIntOrNull()
    if (intValue != null) return intValue
    val doubleValue = input.toDoubleOrNull()
    if (doubleValue != null) return doubleValue
    return input
}

// Hierarchical JSON Node traverser and updater
private fun updateJsonByPath(json: Any, path: List<String>, newValue: Any) {
    android.util.Log.d("NetworkInterceptor", "[ResponseEditorScreen] updateJsonByPath: class=${json.javaClass.simpleName}, path=$path, newValue=$newValue")
    if (path.isEmpty()) return
    val currentKey = path.first()

    if (path.size == 1) {
        if (json is JSONObject) {
            json.put(currentKey, newValue)
        } else if (json is JSONArray) {
            val index = currentKey.removeSurrounding("[", "]").toIntOrNull()
            if (index != null && index in 0 until json.length()) {
                json.put(index, newValue)
            }
        }
        return
    }

    if (json is JSONObject) {
        val child = json.opt(currentKey)
        if (child != null) {
            updateJsonByPath(child, path.drop(1), newValue)
        }
    } else if (json is JSONArray) {
        val index = currentKey.removeSurrounding("[", "]").toIntOrNull()
        if (index != null && index in 0 until json.length()) {
            val child = json.opt(index)
            if (child != null) {
                updateJsonByPath(child, path.drop(1), newValue)
            }
        }
    }
}
