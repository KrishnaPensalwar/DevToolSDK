package com.example.devtool.ui.dashboard.device

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devtool.ui.components.BentoCard
import com.example.devtool.ui.components.BentoSectionHeader
import com.example.devtool.ui.components.SectionLabel
import com.example.devtool.ui.theme.colorStatusError

@Composable
fun DeviceInfoScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val runtime = Runtime.getRuntime()
    val usedMem = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
    val totalMem = runtime.maxMemory() / (1024 * 1024)
    val percent = if (totalMem > 0) (usedMem * 100 / totalMem).toInt() else 0

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero header
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                SectionLabel(text = "Diagnostic Report")
            }
            Text(
                text = "Device Intelligence",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Runtime environment and application metadata for this build.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }

        // App Intelligence card
        BentoCard {
            BentoSectionHeader(title = "Application Context")
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(label = "App Version", value = packageInfo.versionName ?: "Unknown")
            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            InfoRow(label = "Package", value = context.packageName)
            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            InfoRow(label = "Target SDK", value = Build.VERSION.SDK_INT.toString())
        }

        // Device hardware card
        BentoCard {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                BentoSectionHeader(title = "Active Hardware")
            }
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(label = "Manufacturer", value = Build.MANUFACTURER)
            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            InfoRow(label = "Model", value = Build.MODEL)
            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            InfoRow(
                label = "Android",
                value = "API ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})"
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            InfoRow(label = "Hardware", value = Build.HARDWARE)
        }

        // Software layer card
        BentoCard {
            BentoSectionHeader(title = "Software Layer")
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(label = "OS Platform", value = "Android")
            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            InfoRow(label = "Version", value = Build.VERSION.RELEASE)
            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            InfoRow(label = "API Level", value = Build.VERSION.SDK_INT.toString())
        }

        // Runtime memory card
        BentoCard {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                BentoSectionHeader(title = "Runtime Memory")
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "JVM heap usage at the time of capture.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(label = "Used Heap", value = "${usedMem}MB")
            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            InfoRow(label = "Max Heap", value = "${totalMem}MB")
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LinearProgressIndicator(
                    progress = { percent / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (percent > 80) colorStatusError else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            color = if (label == "Package") MaterialTheme.colorScheme.secondary
            else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f)
        )
    }
}
