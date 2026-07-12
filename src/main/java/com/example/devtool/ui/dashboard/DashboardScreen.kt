package com.example.devtool.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.devtool.ui.dashboard.network.NetworkListScreen
import com.example.devtool.ui.dashboard.device.DeviceInfoScreen
import com.example.devtool.ui.dashboard.crash.CrashScreen
import com.example.devtool.ui.dashboard.perf.PerformanceScreen
import com.example.devtool.ui.dashboard.storage.StorageInspectorScreen
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen() {
    var selectedTab by remember { mutableStateOf(DashboardTab.NETWORK) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen, // Disable swipe-to-open
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "DevTool SDK",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)
                )
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                DashboardTab.values().forEach { tab ->
                    NavigationDrawerItem(
                        label = { Text(tab.title) },
                        selected = selectedTab == tab,
                        onClick = {
                            selectedTab = tab
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(selectedTab.title) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { paddingValues ->
            val modifier = Modifier.padding(paddingValues).fillMaxSize()
            when (selectedTab) {
                DashboardTab.NETWORK -> NetworkListScreen(modifier = modifier)
                DashboardTab.CRASHES -> CrashScreen(modifier = modifier)
                DashboardTab.PERFORMANCE -> PerformanceScreen(modifier = modifier)
                DashboardTab.STORAGE -> StorageInspectorScreen(modifier = modifier)
                DashboardTab.DEVICE -> DeviceInfoScreen(modifier = modifier)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit
) {
    CenterAlignedTopAppBar(
        title = title,
        navigationIcon = navigationIcon,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        )
    )
}

enum class DashboardTab(val title: String, val icon: ImageVector) {
    NETWORK("Network", Icons.Default.Http),
    CRASHES("Crashes", Icons.Default.BugReport),
    PERFORMANCE("Performance", Icons.Default.Speed),
    STORAGE("Storage", Icons.Default.Storage),
    DEVICE("Device Info", Icons.Default.Devices)
}
