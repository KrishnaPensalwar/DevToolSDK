package com.example.devtool.ui.dashboard.device

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devtool.ui.theme.*

@Composable
fun DeviceInfoScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(sdkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InfoSection(title = "App Information") {
            DeviceInfoRow("Version", packageInfo.versionName ?: "Unknown")
            DeviceInfoRow("Package", context.packageName)
            DeviceInfoRow("Build", if (android.provider.Settings.Global.getInt(context.contentResolver, android.provider.Settings.Global.ADB_ENABLED, 0) == 1) "Debug" else "Release")
        }

        InfoSection(title = "Device Details") {
            DeviceInfoRow("Manufacturer", Build.MANUFACTURER)
            DeviceInfoRow("Model", Build.MODEL)
            DeviceInfoRow("Android", Build.VERSION.RELEASE)
            DeviceInfoRow("SDK", Build.VERSION.SDK_INT.toString())
        }
    }
}

@Composable
fun InfoSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = sdkSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = sdkPrimary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun DeviceInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = sdkOnSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = sdkOnSurface)
    }
}
