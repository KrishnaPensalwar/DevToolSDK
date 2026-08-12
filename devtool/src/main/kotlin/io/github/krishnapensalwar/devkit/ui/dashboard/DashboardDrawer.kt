package io.github.krishnapensalwar.devkit.ui.dashboard

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.krishnapensalwar.devkit.ui.theme.sdkBackground
import io.github.krishnapensalwar.devkit.ui.theme.sdkOnSurface
import io.github.krishnapensalwar.devkit.ui.theme.sdkOnSurfaceVariant
import io.github.krishnapensalwar.devkit.ui.theme.sdkPrimary
import io.github.krishnapensalwar.devkit.ui.theme.sdkSurfaceVariant

@Composable
internal fun DashboardDrawer(
    visibleTabs: List<DashboardTab>,
    currentTab: DashboardTab?,
    onTabSelected: (DashboardTab) -> Unit,
    onBackToApp: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.fillMaxHeight(),
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
                onClick = { onTabSelected(tab) },
                label = { Text(tab.title) },
                icon = { Icon(tab.icon, contentDescription = tab.title) },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = sdkSurfaceVariant,
                    unselectedContainerColor = Color.Transparent,
                    selectedIconColor = sdkPrimary,
                    selectedTextColor = sdkPrimary
                )
            )
        }

        Spacer(Modifier.weight(1f))

        HorizontalDivider()

        NavigationDrawerItem(
            selected = false,
            onClick = onBackToApp,
            label = { Text("Back to app") },
            icon = {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Back to app"
                )
            },
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = Color.Transparent,
                unselectedIconColor = sdkOnSurfaceVariant,
                unselectedTextColor = sdkOnSurface
            )
        )

        Spacer(Modifier.height(16.dp))
    }
}
