package io.github.krishnapensalwar.devkit.ui.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.krishnapensalwar.devkit.DevTool
import io.github.krishnapensalwar.devkit.ui.navigation.Destination
import io.github.krishnapensalwar.devkit.ui.navigation.navigateTo
import io.github.krishnapensalwar.devkit.ui.theme.sdkBackground
import io.github.krishnapensalwar.devkit.ui.theme.sdkOnSurface
import io.github.krishnapensalwar.devkit.ui.theme.sdkPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DashboardScreen(onClose: () -> Unit) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val visibleTabs = remember {
        DashboardTab.entries.filter { tab ->
            when (tab) {
                DashboardTab.NETWORK, DashboardTab.ANALYTICS -> DevTool.config.isNetworkMonitoringEnabled
                DashboardTab.CRASHES -> DevTool.config.isCrashReportingEnabled
                else -> true
            }
        }
    }

    val startRoute = remember(visibleTabs) {
        (visibleTabs.firstOrNull() ?: DashboardTab.HOME).name.lowercase()
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: startRoute
    val currentTab = visibleTabs.firstOrNull { it.name.lowercase() == currentRoute }
    val isDashboardTab = currentTab != null

    fun closeDrawerThen(action: () -> Unit) {
        scope.launch {
            drawerState.close()
            action()
        }
    }

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    BackHandler(enabled = !drawerState.isOpen && isDashboardTab) {
        onClose()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen || isDashboardTab,
        drawerContent = {
            DashboardDrawer(
                visibleTabs = visibleTabs,
                currentTab = currentTab,
                onTabSelected = { tab ->
                    navController.navigateTo(Destination.Tab(tab))
                    scope.launch { drawerState.close() }
                },
                onBackToApp = { closeDrawerThen(onClose) }
            )
        }
    ) {
        Scaffold(
            containerColor = sdkBackground,
            topBar = {
                if (currentTab != null) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(currentTab.title, fontWeight = FontWeight.Bold)
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } }
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "Open menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = onClose) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Back to app"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = sdkBackground,
                            titleContentColor = sdkPrimary,
                            navigationIconContentColor = sdkOnSurface,
                            actionIconContentColor = sdkOnSurface
                        )
                    )
                }
            }
        ) { padding ->
            DashboardNavGraph(
                navController = navController,
                startRoute = startRoute,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            )
        }
    }
}

internal enum class DashboardTab(val title: String, val icon: ImageVector) {
    HOME("Overview", Icons.Default.Dashboard),
    NETWORK("Network", Icons.Default.Http),
    ANALYTICS("Analytics", Icons.Default.Analytics),
    CRASHES("Crashes", Icons.Default.BugReport),
    STORAGE("Storage", Icons.Default.Storage),
    DEVICE("Device Info", Icons.Default.Devices)
}
