package com.example.devtool

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Simple overview screen for the DevTool SDK.
 * Displays a switch that enables or disables mocking globally.
 */
@Composable
fun DevToolOverviewScreen() {
    // Remember the current mocking state
    val isMockingEnabled: MutableState<Boolean> = remember { mutableStateOf(DevToolSdk.isMockingEnabled()) }

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
            }
        }
    )
}
