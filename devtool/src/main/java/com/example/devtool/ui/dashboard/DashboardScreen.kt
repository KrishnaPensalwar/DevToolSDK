package com.example.devtool.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devtool.DevToolOverviewScreen
import com.example.devtool.ui.dashboard.network.NetworkListScreen
import com.example.devtool.ui.dashboard.network.AnalyticsScreen
import com.example.devtool.ui.dashboard.device.DeviceInfoScreen
import com.example.devtool.ui.dashboard.crash.CrashScreen
import com.example.devtool.ui.dashboard.perf.PerformanceScreen
import com.example.devtool.ui.dashboard.storage.StorageInspectorScreen
import com.example.devtool.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    var selectedTab by remember { mutableStateOf(DashboardTab.NETWORK) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = sdkBackground,
                drawerContentColor = sdkOnSurface
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "DevTool SDK",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
                    color = sdkPrimary
                )
                HorizontalDivider(color = sdkSurfaceVariant, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))
                DashboardTab.entries.forEach { tab ->
                    NavigationDrawerItem(
                        label = { Text(tab.title) },
                        selected = selectedTab == tab,
                        onClick = {
                            selectedTab = tab
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = sdkSurfaceVariant,
                            unselectedContainerColor = Color.Transparent,
                            selectedIconColor = sdkPrimary,
                            unselectedIconColor = sdkOnSurfaceVariant,
                            selectedTextColor = sdkPrimary,
                            unselectedTextColor = sdkOnSurfaceVariant
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            containerColor = sdkBackground,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = selectedTab.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(sdkSurface)
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = sdkBackground,
                        titleContentColor = sdkPrimary,
                        navigationIconContentColor = sdkOnSurface
                    )
                )
            }
        ) { paddingValues ->
            val modifier = Modifier.padding(paddingValues).fillMaxSize()
            when (selectedTab) {
                DashboardTab.HOME -> DevToolOverviewScreen()
                DashboardTab.NETWORK -> NetworkListScreen(modifier = modifier)
                DashboardTab.ANALYTICS -> AnalyticsScreen(modifier = modifier)
                DashboardTab.CRASHES -> CrashScreen(modifier = modifier)
                DashboardTab.PERFORMANCE -> PerformanceScreen(modifier = modifier)
                DashboardTab.STORAGE -> StorageInspectorScreen(modifier = modifier)
                DashboardTab.DEVICE -> DeviceInfoScreen(modifier = modifier)
            }
        }
    }
}

enum class DashboardTab(val title: String, val icon: ImageVector) {
    HOME("Overview", Icons.Default.Dashboard),
    NETWORK("Network", Icons.Default.Http),
    ANALYTICS("Analytics", Icons.Default.Analytics),
    CRASHES("Crashes", Icons.Default.BugReport),
    PERFORMANCE("Performance", Icons.Default.Speed),
    STORAGE("Storage", Icons.Default.Storage),
    DEVICE("Device Info", Icons.Default.Devices)
}
