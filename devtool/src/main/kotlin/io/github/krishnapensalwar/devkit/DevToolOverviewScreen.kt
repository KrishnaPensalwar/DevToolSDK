package io.github.krishnapensalwar.devkit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.krishnapensalwar.devkit.ui.CacheScreen

/**
 * Simple overview screen for the DevTool SDK.
 * Displays a switch that enables or disables mocking globally.
 */
@Composable
fun DevToolOverviewScreen() {
    val scope = rememberCoroutineScope()
    var showCacheScreen by remember { mutableStateOf(false) }
    // Remember the current mocking state
    val isMockingEnabled = remember { mutableStateOf(DevToolSdk.isMockingEnabled()) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = "DevTool Mocking",
                    style = MaterialTheme.typography.headlineMedium
                )
                Switch(
                    checked = isMockingEnabled.value,
                    onCheckedChange = { enabled ->
                        isMockingEnabled.value = enabled
                        DevToolSdk.setMockingEnabled(enabled)
                    },
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = if (isMockingEnabled.value) "Mocking is enabled" else "Mocking is disabled",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Button(
                    onClick = { showCacheScreen = true },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("View Cached Responses")
                }
                Button(
                    onClick = { scope.launch { DevToolSdk.clearCache() } },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Clear Cache")
                }
            }
        }
    )
    // Show CacheScreen when button clicked
    if (showCacheScreen) {
        CacheScreen(onClose = { showCacheScreen = false })
    }
}