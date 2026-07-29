package io.github.krishnapensalwar.devkit.ui.dashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.krishnapensalwar.devkit.ui.theme.DevToolTheme

/**
 * Main Activity hosting the DevTool SDK Jetpack Compose dashboard interface.
 */
class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DevToolTheme {
                DashboardScreen()
            }
        }
    }

    companion object {
        /**
         * Creates an [Intent] to launch the [DashboardActivity].
         *
         * @param context Host application or component context.
         * @return Intent configured to open the DevTool dashboard.
         */
        fun newIntent(context: Context): Intent {
            return Intent(context, DashboardActivity::class.java)
        }
    }
}