package io.github.krishnapensalwar.devkit.ui.navigation

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
