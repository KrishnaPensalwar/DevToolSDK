package com.example.devtool.ui.dashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.example.devtool.ui.theme.DevToolTheme

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
        fun newIntent(context: Context): Intent {
            return Intent(context, DashboardActivity::class.java)
        }
    }
}
