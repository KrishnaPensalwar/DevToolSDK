package io.github.krishnapensalwar.devkit.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.entryProvider
import io.github.krishnapensalwar.devkit.DevToolOverviewScreen
import io.github.krishnapensalwar.devkit.DevToolSdk
import io.github.krishnapensalwar.devkit.core.logging.LoggerManager
import io.github.krishnapensalwar.devkit.ui.CacheScreen
import io.github.krishnapensalwar.devkit.ui.dashboard.network.NetworkListScreen
import io.github.krishnapensalwar.devkit.ui.dashboard.network.NetworkDetailScreen
import io.github.krishnapensalwar.devkit.ui.dashboard.network.ResponseEditorScreen
import io.github.krishnapensalwar.devkit.ui.dashboard.network.AnalyticsScreen
import io.github.krishnapensalwar.devkit.ui.dashboard.device.DeviceInfoScreen
import io.github.krishnapensalwar.devkit.ui.dashboard.crash.CrashScreen
import io.github.krishnapensalwar.devkit.ui.dashboard.perf.PerformanceScreen
import io.github.krishnapensalwar.devkit.ui.dashboard.storage.StorageInspectorScreen
import io.github.krishnapensalwar.devkit.ui.navigation.Destination
import io.github.krishnapensalwar.devkit.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val backStack = remember { mutableStateListOf<Any>(Destination.Tab(DashboardTab.NETWORK)) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val currentDestination = backStack.lastOrNull() ?: Destination.Tab(DashboardTab.NETWORK)
    val showParentTopBar = currentDestination is Destination.Tab
    val activeTab = when (currentDestination) {
        is Destination.Tab -> currentDestination.tab
        else -> {
            // Find root/bottom-most tab
            (backStack.firstOrNull() as? Destination.Tab)?.tab ?: DashboardTab.NETWORK
        }
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
                        selected = activeTab == tab,
                        onClick = {
                            backStack.clear()
                            backStack.add(Destination.Tab(tab))
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
                    val tab = (currentDestination as Destination.Tab).tab
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
            NavDisplay(
                backStack = backStack,
                onBack = {
                    if (backStack.size > 1) {
                        backStack.removeLast()
                    }
                },
                entryProvider = entryProvider {
                    entry<Destination.Tab> { key ->
                        val tab = key.tab
                        when (tab) {
                            DashboardTab.HOME -> DevToolOverviewScreen(backStack = backStack)
                            DashboardTab.NETWORK -> NetworkListScreen(backStack = backStack, modifier = modifier)
                            DashboardTab.ANALYTICS -> AnalyticsScreen(modifier = modifier)
                            DashboardTab.CRASHES -> CrashScreen(modifier = modifier)
                            DashboardTab.PERFORMANCE -> PerformanceScreen(modifier = modifier)
                            DashboardTab.STORAGE -> StorageInspectorScreen(modifier = modifier)
                            DashboardTab.DEVICE -> DeviceInfoScreen(modifier = modifier)
                        }
                    }
                    entry<Destination.CacheList> {
                        CacheScreen(backStack = backStack)
                    }
                    entry<Destination.ResponseEditor> { key ->
                        ResponseEditorScreen(
                            initialBody = key.initialBody,
                            endpoint = "${key.method} ${key.url}",
                            onDismiss = { backStack.removeLast() },
                            onSave = { newBody ->
                                scope.launch {
                                    DevToolSdk.updateCachedResponse(key.url, key.method, newBody)
                                }
                                backStack.removeLast()
                            }
                        )
                    }
                    entry<Destination.NetworkDetail> { key ->
                        val repository = LoggerManager.getNetworkRepository()
                        val calls by repository.calls.collectAsState()
                        val call = remember(calls, key.callId) { calls.find { it.id == key.callId } }
                        if (call != null) {
                            NetworkDetailScreen(
                                call = call,
                                onDismiss = { backStack.removeLast() },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                backStack.removeLast()
                            }
                        }
                    }
                }
            )
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