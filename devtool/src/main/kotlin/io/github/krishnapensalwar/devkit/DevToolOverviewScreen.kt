package io.github.krishnapensalwar.devkit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import io.github.krishnapensalwar.devkit.ui.navigation.Destination
import io.github.krishnapensalwar.devkit.ui.navigation.navigateTo
import io.github.krishnapensalwar.devkit.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Overview screen for the DevTool SDK.
 * Displays a switch that enables or disables mocking globally and displays config status.
 */
@Composable
fun DevToolOverviewScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val isMockingEnabled = remember { mutableStateOf(DevToolSdk.isMockingEnabled()) }

    Column(
        modifier = modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // SDK Status Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            )
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text(
                            text = "SDK Feature Status",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = sdkPrimary,

                            )
                        Spacer(Modifier.height(12.dp))
                        FeatureStatusRow(
                            name = "Network Monitoring",
                            enabled = DevTool.config.isNetworkMonitoringEnabled
                        )
                        Spacer(Modifier.height(6.dp))
                        FeatureStatusRow(
                            name = "Crash Reporting",
                            enabled = DevTool.config.isCrashReportingEnabled
                        )
                        Spacer(Modifier.height(6.dp))

                        //   FeatureStatusRow(name = "Performance Tracking", enabled = DevTool.config.isPerformanceMonitoringEnabled)

                    }

                }

            }
        }

        // Mocking config card (only if Network Monitoring is enabled)
        if (DevTool.config.isNetworkMonitoringEnabled) {
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
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
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
        }

        Spacer(modifier = Modifier.weight(1f))

        // Cache actions (only if Network Monitoring is enabled)
        if (DevTool.config.isNetworkMonitoringEnabled) {
            Button(
                onClick = { navController.navigateTo(Destination.CacheList) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Cached Responses")
            }

            val showClearCacheDialog = remember { mutableStateOf(false) }

            OutlinedButton(
                onClick = { showClearCacheDialog.value = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear Cache")
            }

            if (showClearCacheDialog.value) {
                AlertDialog(
                    onDismissRequest = { showClearCacheDialog.value = false },
                    title = {
                        Text(
                            text = "Clear Cache",
                            fontWeight = FontWeight.Bold,
                            color = sdkOnSurface
                        )
                    },
                    text = {
                        Text(
                            text = "Are you sure you want to clear all cached responses? This action cannot be undone.",
                            color = sdkOnSurfaceVariant
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    DevToolSdk.clearCache()
                                }
                                showClearCacheDialog.value = false
                            }
                        ) {
                            Text("Clear", color = colorStatusError)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showClearCacheDialog.value = false }
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
        }
    }
}

@Composable
fun FeatureStatusRow(name: String, enabled: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = if (enabled) "Active" else "Inactive",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) colorStatusSuccess else colorStatusNeutral
        )
    }
}