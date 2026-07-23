package io.github.krishnapensalwar.devkit.ui.dashboard.network

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.krishnapensalwar.devkit.network.model.NetworkCall
import io.github.krishnapensalwar.devkit.ui.components.StatusDot
import io.github.krishnapensalwar.devkit.ui.dashboard.network.methodColor
import io.github.krishnapensalwar.devkit.ui.theme.*
import java.util.Locale

@Composable
fun OverviewContent(
    call: NetworkCall
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        DetailRow(label = "URL", value = call.url)
        
        DetailRow(
            label = "METHOD",
            valueContent = {
                Surface(
                    color = methodColor(call.method).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50),
                ) {
                    Text(
                        text = call.method,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = methodColor(call.method),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        )

        DetailRow(
            label = "STATUS",
            valueContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusDot(
                        color = when {
                            call.statusCode in 200..299 -> colorStatusSuccess
                            call.statusCode >= 400 -> colorStatusError
                            else -> colorStatusNeutral
                        }
                    )
                    Text(
                        text = call.statusCode.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = when {
                            call.statusCode in 200..299 -> colorStatusSuccess
                            call.statusCode >= 400 -> colorStatusError
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        DetailRow(
            label = "DURATION", 
            value = if (call.duration >= 1000) String.format(Locale.getDefault(), "%.3f s", call.duration / 1000.0) else "${call.duration} ms"
        )
        DetailRow(
            label = "SIZE", 
            value = formatSize(call.responseSize)
        )
    }
}

private fun formatSize(bytes: Long): String = when {
    bytes >= 1_048_576 -> String.format(Locale.getDefault(), "%.1f MB", bytes / 1_048_576.0)
    bytes >= 1024 -> String.format(Locale.getDefault(), "%d bytes", bytes)
    else -> "${bytes} bytes"
}