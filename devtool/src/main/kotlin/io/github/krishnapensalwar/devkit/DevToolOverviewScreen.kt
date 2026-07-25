package io.github.krishnapensalwar.devkit

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.krishnapensalwar.devkit.ui.navigation.Destination
import kotlinx.coroutines.launch

/**
 * Overview screen for the DevTool SDK.
 * Displays a switch that enables or disables mocking globally.
 */
@Composable
fun DevToolOverviewScreen(
    backStack: SnapshotStateList<Any>
) {
    val scope = rememberCoroutineScope()
    val isMockingEnabled = remember { mutableStateOf(DevToolSdk.isMockingEnabled()) }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "DevTool Overview",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Mocking config card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mock Network Traffic",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isMockingEnabled.value) "Mocking is currently enabled" else "Mocking is currently disabled",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isMockingEnabled.value,
                            onCheckedChange = { enabled ->
                                isMockingEnabled.value = enabled
                                DevToolSdk.setMockingEnabled(enabled)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Actions Section
            Button(
                onClick = { backStack.add(Destination.CacheList) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Cached Responses")
            }

            OutlinedButton(
                onClick = { scope.launch { DevToolSdk.clearCache() } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear Cache")
            }
        }
    }
}