package io.github.krishnapensalwar.devkit.ui.dashboard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
import io.github.krishnapensalwar.devkit.ui.dashboard.storage.StorageInspectorScreen
import io.github.krishnapensalwar.devkit.ui.navigation.pop
import kotlinx.coroutines.launch

@Composable
internal fun DashboardNavGraph(
    navController: NavHostController,
    startRoute: String,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    NavHost(
        modifier = modifier,
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
            val url = previous?.savedStateHandle?.get<String>("url").orEmpty()
            val method = previous?.savedStateHandle?.get<String>("method").orEmpty()
            val body = previous?.savedStateHandle?.get<String>("initialBody").orEmpty()

            ResponseEditorScreen(
                initialBody = body,
                endpoint = "$method $url",
                navController = navController,
                onSave = {
                    scope.launch {
                        DevToolSdk.updateCachedResponse(url, method, it)
                    }
                }
            )
        }

        composable("network_detail/{callId}") { entry ->
            val callId = entry.arguments?.getString("callId")?.toLongOrNull() ?: 0L
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
