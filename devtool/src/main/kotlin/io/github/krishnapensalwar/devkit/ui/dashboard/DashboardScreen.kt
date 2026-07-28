package io.github.krishnapensalwar.devkit.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.krishnapensalwar.devkit.DevToolOverviewScreen
import io.github.krishnapensalwar.devkit.DevToolSdk
import io.github.krishnapensalwar.devkit.core.logging.LoggerManager
import io.github.krishnapensalwar.devkit.ui.CacheScreen
import io.github.krishnapensalwar.devkit.ui.dashboard.crash.CrashScreen
import io.github.krishnapensalwar.devkit.ui.dashboard.device.DeviceInfoScreen
import io.github.krishnapensalwar.devkit.ui.dashboard.network.AnalyticsScreen
import io.github.krishnapensalwar.devkit.ui.dashboard.network.NetworkDetailScreen
import io.github.krishnapensalwar.devkit.ui.dashboard.network.NetworkListScreen
import io.github.krishnapensalwar.devkit.ui.dashboard.network.ResponseEditorScreen
import io.github.krishnapensalwar.devkit.ui.dashboard.perf.PerformanceScreen
import io.github.krishnapensalwar.devkit.ui.dashboard.storage.StorageInspectorScreen
import io.github.krishnapensalwar.devkit.ui.navigation.Destination
import io.github.krishnapensalwar.devkit.ui.navigation.pop
import io.github.krishnapensalwar.devkit.ui.theme.sdkBackground
import io.github.krishnapensalwar.devkit.ui.theme.sdkOnSurface
import io.github.krishnapensalwar.devkit.ui.theme.sdkOnSurfaceVariant
import io.github.krishnapensalwar.devkit.ui.theme.sdkPrimary
import io.github.krishnapensalwar.devkit.ui.theme.sdkSurface
import io.github.krishnapensalwar.devkit.ui.theme.sdkSurfaceVariant
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "tab/${DashboardTab.NETWORK.name}"
    val showParentTopBar = currentRoute.startsWith("tab/")
    val activeTab = if (showParentTopBar) {
        val tabName = currentRoute.substringAfter("tab/")
        DashboardTab.entries.find { it.name == tabName } ?: DashboardTab.NETWORK
    } else {
        DashboardTab.NETWORK
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen && showParentTopBar,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = sdkBackground,
                drawerContentColor = sdkOnSurface
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "DevTool SDK",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
                    color = sdkPrimary
                )
                HorizontalDivider(color = sdkSurfaceVariant, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))
                DashboardTab.entries.forEach { tab ->
                    NavigationDrawerItem(
                        label = { Text(tab.title) },
                        selected = activeTab == tab,
                        onClick = {
                            navController.navigate(Destination.Tab(tab))
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
                if (showParentTopBar) {
                    val tabName = currentRoute.substringAfter("tab/")
                    val tab = DashboardTab.entries.find { it.name == tabName } ?: DashboardTab.NETWORK
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = tab.title,
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
            }
        ) { paddingValues ->
            val modifier = Modifier.padding(paddingValues).fillMaxSize()
            NavHost(
                navController = navController,
                startDestination = "tab/${DashboardTab.NETWORK.name}"
            ) {
                composable("tab/{tabName}") { backStackEntry ->
                    val tabName = backStackEntry.arguments?.getString("tabName")
                    val tab = DashboardTab.entries.find { it.name == tabName } ?: DashboardTab.NETWORK
                    when (tab) {
                        DashboardTab.HOME -> DevToolOverviewScreen(navController = navController, modifier = modifier)
                        DashboardTab.NETWORK -> NetworkListScreen(navController = navController, modifier = modifier)
                        DashboardTab.ANALYTICS -> AnalyticsScreen(navController = navController, modifier = modifier)
                        DashboardTab.CRASHES -> CrashScreen(navController = navController, modifier = modifier)
                        DashboardTab.PERFORMANCE -> PerformanceScreen(navController = navController, modifier = modifier)
                        DashboardTab.STORAGE -> StorageInspectorScreen(navController = navController, modifier = modifier)
                        DashboardTab.DEVICE -> DeviceInfoScreen(navController = navController, modifier = modifier)
                    }
                }
                composable("cache_list") {
                    CacheScreen(navController = navController)
                }
                composable("response_editor") {
                    val previousEntry = remember { navController.previousBackStackEntry }
                    val url = previousEntry?.savedStateHandle?.get<String>("url") ?: ""
                    val method = previousEntry?.savedStateHandle?.get<String>("method") ?: ""
                    val initialBody = previousEntry?.savedStateHandle?.get<String>("initialBody") ?: ""
                    ResponseEditorScreen(
                        initialBody = initialBody,
                        endpoint = "$method $url",
                        navController = navController,
                        onSave = { newBody ->
                            scope.launch {
                                DevToolSdk.updateCachedResponse(url, method, newBody)
                            }
                        }
                    )
                }
                composable("network_detail/{callId}") { backStackEntry ->
                    val callId = backStackEntry.arguments?.getString("callId")?.toLongOrNull() ?: 0L
                    val repository = LoggerManager.getNetworkRepository()
                    val calls by repository.calls.collectAsState()
                    val call = remember(calls, callId) { calls.find { it.id == callId } }
                    if (call != null) {
                        NetworkDetailScreen(
                            call = call,
                            navController = navController,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            navController.pop()
                        }
                    }
                }
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