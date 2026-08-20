package io.github.krishnapensalwar.devkit.ui.navigation

import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import io.github.krishnapensalwar.devkit.ui.dashboard.DashboardTab

internal sealed interface Destination {
    data class Tab(val tab: DashboardTab) : Destination
    object CacheList : Destination
    data class ResponseEditor(
        val url: String,
        val method: String,
        val initialBody: String,
        val initialStatus: Int
    ) : Destination
    data class NetworkDetail(val callId: Long) : Destination
}

internal fun NavController.navigateTo(destination: Destination) {
    when (destination) {
        is Destination.Tab -> {

            navigate(destination.tab.name.lowercase()) {
                popUpTo(graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        Destination.CacheList -> navigate("cache_list")

        is Destination.ResponseEditor -> {
            currentBackStackEntry?.savedStateHandle?.set("url", destination.url)
            currentBackStackEntry?.savedStateHandle?.set("method", destination.method)
            currentBackStackEntry?.savedStateHandle?.set("initialBody", destination.initialBody)
            currentBackStackEntry?.savedStateHandle?.set("initialStatus", destination.initialStatus)
            navigate("response_editor")
        }

        is Destination.NetworkDetail -> navigate("network_detail/${destination.callId}")
    }
}

internal fun NavController.pop() {
    popBackStack()
}
