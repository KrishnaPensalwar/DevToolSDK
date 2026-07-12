package com.example.devtool.ui.dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.devtool.ui.LogDashboard
import com.example.devtool.ui.dashboard.network.NetworkListScreen
import com.example.devtool.ui.dashboard.device.DeviceInfoScreen

@Composable
fun DashboardScreen() {
    var selectedTab by remember { mutableStateOf(DashboardTab.LOGS) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                DashboardTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        val modifier = Modifier.padding(paddingValues)
        when (selectedTab) {
            DashboardTab.LOGS -> LogDashboard()
            DashboardTab.NETWORK -> NetworkListScreen(modifier = modifier)
            DashboardTab.CRASHES -> Text("Crashes Screen", modifier = modifier)
            DashboardTab.PERFORMANCE -> Text("Performance Screen", modifier = modifier)
            DashboardTab.DEVICE -> DeviceInfoScreen(modifier = modifier)
        }
    }
}

enum class DashboardTab(val title: String, val icon: ImageVector) {
    LOGS("Logs", Icons.Default.List),
    NETWORK("Network", Icons.Default.Http),
    CRASHES("Crashes", Icons.Default.BugReport),
    PERFORMANCE("Perf", Icons.Default.Speed),
    DEVICE("Device", Icons.Default.Devices)
}
