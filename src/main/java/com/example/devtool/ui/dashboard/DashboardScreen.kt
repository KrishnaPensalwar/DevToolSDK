package com.example.devtool.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devtool.ui.components.DevToolIconButton
import com.example.devtool.ui.dashboard.crash.CrashScreen
import com.example.devtool.ui.dashboard.device.DeviceInfoScreen
import com.example.devtool.ui.dashboard.network.NetworkListScreen
import com.example.devtool.ui.dashboard.storage.StorageInspectorScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    var selectedTab by remember { mutableStateOf(DashboardTab.NETWORK) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = selectedTab.screenTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    DevToolIconButton(onClick = { /* Refresh */ }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DevToolIconButton(onClick = { /* More options */ }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant,
                thickness = 0.5.dp
            )
        },
        bottomBar = {
            Column {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    thickness = 0.5.dp
                )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    DashboardTab.entries.forEach { tab ->
                        val selected = selectedTab == tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    tab.icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    tab.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                            ),
                            modifier = if (selected) {
                                Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                                    )
                            } else Modifier
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        val modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
        when (selectedTab) {
            DashboardTab.NETWORK -> NetworkListScreen(modifier = modifier)
            DashboardTab.STORAGE -> StorageInspectorScreen(modifier = modifier)
            DashboardTab.CRASHES -> CrashScreen(modifier = modifier)
            DashboardTab.DEVICE -> DeviceInfoScreen(modifier = modifier)
        }
    }
}

enum class DashboardTab(
    val title: String,
    val screenTitle: String,
    val icon: ImageVector
) {
    NETWORK("Network", "DevConsole", Icons.Default.SwapVert),
    STORAGE("Storage", "Storage Browser", Icons.Default.Storage),
    CRASHES("Crashes", "Crash Reports", Icons.Default.ErrorOutline),
    DEVICE("Auth", "Device Intelligence", Icons.Default.Security)
}
