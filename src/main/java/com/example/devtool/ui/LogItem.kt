package com.example.devtool.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devtool.core.logging.DevLog
import com.example.devtool.core.logging.LogLevel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LogItem(log: DevLog, modifier: Modifier = Modifier) {
    val backgroundColor = when (log.level) {
        LogLevel.ERROR, LogLevel.CRASH -> Color(0xFFFFEBEE)
        LogLevel.WARN -> Color(0xFFFFFDE7)
        LogLevel.NETWORK -> Color(0xFFE8F5E9)
        LogLevel.PERFORMANCE -> Color(0xFFE3F2FD)
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when (log.level) {
        LogLevel.ERROR, LogLevel.CRASH -> Color.Red
        LogLevel.WARN -> Color(0xFFF57F17)
        LogLevel.NETWORK -> Color(0xFF2E7D32)
        LogLevel.PERFORMANCE -> Color(0xFF1976D2)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = log.level.name,
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(log.timestamp)),
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
            Text(
                text = log.tag,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = log.message,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
