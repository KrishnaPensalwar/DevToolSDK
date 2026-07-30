package io.github.krishnapensalwar.devkit.ui.dashboard

import android.util.Log
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
import androidx.compose.material3.*
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
import io.github.krishnapensalwar.devkit.DevTool
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
import io.github.krishnapensalwar.devkit.ui.navigation.navigateTo
import io.github.krishnapensalwar.devkit.ui.navigation.pop
import io.github.krishnapensalwar.devkit.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DashboardScreen() {

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val visibleTabs = remember {
        DashboardTab.entries.filter { tab ->
            when (tab) {
                DashboardTab.NETWORK, DashboardTab.ANALYTICS -> DevTool.config.isNetworkMonitoringEnabled
                DashboardTab.CRASHES -> DevTool.config.isCrashReportingEnabled
//                DashboardTab.PERFORMANCE -> DevTool.config.isPerformanceMonitoringEnabled
                else -> true
            }
        }
    }

    val startRoute = remember(visibleTabs) {
        (visibleTabs.firstOrNull() ?: DashboardTab.HOME).name.lowercase()
    }

    val backStackEntry by navController.currentBackStackEntryAsState()

    val currentRoute = backStackEntry?.destination?.route ?: startRoute

    val currentTab = visibleTabs.firstOrNull {
        it.name.lowercase() == currentRoute
    }

    val isDashboardTab = currentTab != null

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen || isDashboardTab,
        drawerContent = {

            ModalDrawerSheet(
                drawerContainerColor = sdkBackground,
                drawerContentColor = sdkOnSurface
            ) {

                Spacer(Modifier.height(24.dp))

                Text(
                    "DevTool SDK",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
                    color = sdkPrimary
                )

                HorizontalDivider()

                Spacer(Modifier.height(12.dp))

                visibleTabs.forEach { tab ->

                    NavigationDrawerItem(
                        selected = currentTab == tab,
                        onClick = {
                            navController.navigateTo(Destination.Tab(tab))
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        label = {
                            Text(tab.title)
                        },
                        icon = {
                            Icon(tab.icon, null)
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = sdkSurfaceVariant,
                            unselectedContainerColor = Color.Transparent,
                            selectedIconColor = sdkPrimary,
                            selectedTextColor = sdkPrimary
                        )
                    )
                }
            }
        }
    ) {

        Scaffold(
            containerColor = sdkBackground,

            topBar = {

                if (currentTab != null) {

                    CenterAlignedTopAppBar(

                        title = {
                            Text(
                                currentTab.title,
                                fontWeight = FontWeight.Bold
                            )
                        },

                        navigationIcon = {

                            IconButton(
                                onClick = {
                                    scope.launch {
                                        drawerState.open()
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Menu, null)
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

        ) { padding ->

            NavHost(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                navController = navController,
                startDestination = startRoute
            ) {

                composable(DashboardTab.HOME.name.lowercase()) {
                    DevToolOverviewScreen(navController, Modifier.fillMaxSize())
                }

                composable(DashboardTab.NETWORK.name.lowercase()) {
                    NetworkListScreen(navController, Modifier.fillMaxSize())
                }

                composable(DashboardTab.ANALYTICS.name.lowercase()) {
                    AnalyticsScreen(navController, Modifier.fillMaxSize())
                }

                composable(DashboardTab.CRASHES.name.lowercase()) {
                    CrashScreen(navController, Modifier.fillMaxSize())
                }

//                composable(DashboardTab.PERFORMANCE.name.lowercase()) {
//                    PerformanceScreen(navController, Modifier.fillMaxSize())
//                }

                composable(DashboardTab.STORAGE.name.lowercase()) {
                    StorageInspectorScreen(navController, Modifier.fillMaxSize())
                }

                composable(DashboardTab.DEVICE.name.lowercase()) {
                    DeviceInfoScreen(navController, Modifier.fillMaxSize())
                }

                composable("cache_list") {
                    CacheScreen(navController)
                }

                composable("response_editor") {

                    val previous = navController.previousBackStackEntry

                    val url =
                        previous?.savedStateHandle?.get<String>("url").orEmpty()

                    val method =
                        previous?.savedStateHandle?.get<String>("method").orEmpty()

                    val body =
                        previous?.savedStateHandle?.get<String>("initialBody").orEmpty()

                    ResponseEditorScreen(
                        initialBody = body,
                        endpoint = "$method $url",
                        navController = navController,
                        onSave = {
                            scope.launch {
                                DevToolSdk.updateCachedResponse(
                                    url,
                                    method,
                                    it
                                )
                            }
                        }
                    )
                }

                composable("network_detail/{callId}") { entry ->

                    val callId =
                        entry.arguments?.getString("callId")?.toLongOrNull() ?: 0L

                    val repository = LoggerManager.getNetworkRepository()

                    val calls by repository.calls.collectAsState()

                    val call = calls.find { it.id == callId }

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

internal enum class DashboardTab(val title: String, val icon: ImageVector) {
    HOME("Overview", Icons.Default.Dashboard),
    NETWORK("Network", Icons.Default.Http),
    ANALYTICS("Analytics", Icons.Default.Analytics),
    CRASHES("Crashes", Icons.Default.BugReport),
//    PERFORMANCE("Performance", Icons.Default.Speed),
    STORAGE("Storage", Icons.Default.Storage),
    DEVICE("Device Info", Icons.Default.Devices)
}
