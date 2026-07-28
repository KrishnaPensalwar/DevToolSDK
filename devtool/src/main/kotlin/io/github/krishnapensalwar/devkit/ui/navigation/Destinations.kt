package io.github.krishnapensalwar.devkit.ui.navigation

import androidx.navigation.NavController
import io.github.krishnapensalwar.devkit.ui.dashboard.DashboardTab

sealed interface Destination {
    data class Tab(val tab: DashboardTab) : Destination
    object CacheList : Destination
    data class ResponseEditor(
        val url: String,
        val method: String,
        val initialBody: String
    ) : Destination
    data class NetworkDetail(val callId: Long) : Destination
}

fun NavController.navigate(destination: Destination) {
    when (destination) {
        is Destination.Tab -> navigate("tab/${destination.tab.name}") {
            popUpTo("tab/${DashboardTab.NETWORK.name}") {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
        Destination.CacheList -> navigate("cache_list")
        is Destination.ResponseEditor -> {
            currentBackStackEntry?.savedStateHandle?.set("url", destination.url)
            currentBackStackEntry?.savedStateHandle?.set("method", destination.method)
            currentBackStackEntry?.savedStateHandle?.set("initialBody", destination.initialBody)
            navigate("response_editor")
        }
        is Destination.NetworkDetail -> navigate("network_detail/${destination.callId}")
    }
}

fun NavController.pop() {
    popBackStack()
}
