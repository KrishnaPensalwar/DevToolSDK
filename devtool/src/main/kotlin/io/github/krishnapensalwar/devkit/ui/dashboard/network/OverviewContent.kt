package io.github.krishnapensalwar.devkit.ui.dashboard.network

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.krishnapensalwar.devkit.network.model.NetworkCall
import io.github.krishnapensalwar.devkit.ui.theme.colorStatusError
import io.github.krishnapensalwar.devkit.ui.theme.colorStatusSuccess
import java.util.Locale

@Composable
internal fun OverviewContent(
    call: NetworkCall
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DetailRow(label = "URL", value = call.url)

        DetailRow(
            label = "METHOD",
            value = call.method,
            valueTextStyle = MaterialTheme.typography.bodyLarge.copy(
                color = methodColor(call.method)
            ),
        )

        DetailRow(
            label = "STATUS",
            value = call.statusCode.toString(),
            valueTextStyle = MaterialTheme.typography.bodyLarge.copy(
                color = when {
                    call.statusCode in 200..299 -> colorStatusSuccess
                    call.statusCode >= 400 -> colorStatusError
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        )

        DetailRow(
            label = "DURATION",
            value = if (call.duration >= 1000) String.format(
                Locale.getDefault(),
                "%.3f s",
                call.duration / 1000.0
            ) else "${call.duration} ms"
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
    else -> "$bytes bytes"
}