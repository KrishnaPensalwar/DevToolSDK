package io.github.krishnapensalwar.devkit.ui.dashboard.network.json

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
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
import org.json.JSONArray

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JsonArrayViewer(
    jsonArray: JSONArray,
    rootName: String,
    searchQuery: String = "",
    path: List<String> = emptyList(),
    activeMatch: JsonMatch? = null,
    onFieldClick: ((List<String>, Any) -> Unit)? = null,
    onImageClick: (String) -> Unit
) {

    var expanded by remember { mutableStateOf(true) }
    val activeHeader = activeMatch.occurrenceAt(path, inKey = true)
    val requester = remember { BringIntoViewRequester() }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank() && jsonContains(jsonArray, searchQuery)) {
            expanded = true
        }
    }
    LaunchedEffect(activeMatch) {
        if (activeMatch.isInside(path)) expanded = true
        if (activeHeader >= 0) requester.bringIntoView()
    }

    Column {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(requester)
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(4.dp))

            HighlightedText(
                text = rootName,
                query = if (path.isEmpty()) "" else searchQuery,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                activeOccurrence = activeHeader
            )

            Text(
                text = " [${jsonArray.length()}]",
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
                for (index in 0 until jsonArray.length()) {
                    JsonNodeViewer(
                        key = "[$index]",
                        value = jsonArray.get(index),
                        searchQuery = searchQuery,
                        path = path + "[$index]",
                        activeMatch = activeMatch,
                        onFieldClick = onFieldClick,
                        onImageClick = onImageClick
                    )
                }
            }
        }
    }
}
