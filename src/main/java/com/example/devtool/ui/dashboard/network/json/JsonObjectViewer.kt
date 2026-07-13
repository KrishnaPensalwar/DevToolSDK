package com.example.devtool.ui.dashboard.network.json

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject

@Composable
fun JsonObjectViewer(
    jsonObject: JSONObject,
    rootName: String? = null,
    searchQuery: String = "",
    onImageClick: (String) -> Unit
) {

    val keys = remember(jsonObject) {
        jsonObject.keys().asSequence().toList()
    }

    val filteredKeys = remember(keys, searchQuery) {
        if (searchQuery.isBlank()) {
            keys
        } else {
            keys.filter { key ->
                key.contains(searchQuery, ignoreCase = true) ||
                        jsonContains(jsonObject.get(key), searchQuery)
            }
        }
    }

    if (filteredKeys.isEmpty() && searchQuery.isNotBlank()) return

    if (rootName == null) {
        Column {
            filteredKeys.forEach { key ->
                JsonNodeViewer(
                    key = key,
                    value = jsonObject.get(key),
                    searchQuery = searchQuery,
                    onImageClick = onImageClick
                )
            }
        }
        return
    }

    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank() && filteredKeys.isNotEmpty()) {
            expanded = true
        }
    }

    Column {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = if (expanded)
                    Icons.Default.KeyboardArrowUp
                else
                    Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.padding(0.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(4.dp))

            HighlightedText(
                text = rootName,
                query = searchQuery,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = " {${filteredKeys.size}}",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(visible = expanded) {

            val lineColor = MaterialTheme.colorScheme.surfaceVariant

            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .drawBehind {
                        drawLine(
                            color = lineColor,
                            start = Offset(0f, 0f),
                            end = Offset(0f, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .padding(start = 8.dp)
            ) {

                filteredKeys.forEach { key ->

                    JsonNodeViewer(
                        key = key,
                        value = jsonObject.get(key),
                        searchQuery = searchQuery,
                        onImageClick = onImageClick
                    )
                }
            }
        }
    }
}