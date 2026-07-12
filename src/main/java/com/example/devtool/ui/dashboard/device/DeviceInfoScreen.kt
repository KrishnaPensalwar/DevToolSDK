package com.example.devtool.ui.dashboard.device

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DeviceInfoScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("App Information", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
        DeviceInfoRow("App Version", packageInfo.versionName ?: "Unknown")
        DeviceInfoRow("Package Name", context.packageName)
        DeviceInfoRow("Build Variant", if (android.provider.Settings.Global.getInt(context.contentResolver, android.provider.Settings.Global.ADB_ENABLED, 0) == 1) "Debug" else "Release")

        Text("Device Information", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        DeviceInfoRow("Manufacturer", Build.MANUFACTURER)
        DeviceInfoRow("Model", Build.MODEL)
        DeviceInfoRow("Android Version", Build.VERSION.RELEASE)
        DeviceInfoRow("SDK Level", Build.VERSION.SDK_INT.toString())
        DeviceInfoRow("Hardware", Build.HARDWARE)
        DeviceInfoRow("Display", Build.DISPLAY)
    }
}

@Composable
fun DeviceInfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        Text(value, fontSize = 14.sp)
    }
}
