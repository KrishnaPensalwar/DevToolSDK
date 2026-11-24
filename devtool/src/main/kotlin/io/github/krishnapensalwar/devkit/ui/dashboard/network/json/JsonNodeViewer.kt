package io.github.krishnapensalwar.devkit.ui.dashboard.network.json

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JsonNodeViewer(
    key: String,
    value: Any,
    searchQuery: String = "",
    path: List<String> = emptyList(),
    activeMatch: JsonMatch? = null,
    onFieldClick: ((List<String>, Any) -> Unit)? = null,
    onImageClick: (String) -> Unit
) {
    when (value) {

        is org.json.JSONObject -> {
            JsonObjectViewer(
                jsonObject = value,
                rootName = key,
                searchQuery = searchQuery,
                path = path,
                activeMatch = activeMatch,
                onFieldClick = onFieldClick,
                onImageClick = onImageClick
            )
        }

        is JSONArray -> {
            JsonArrayViewer(
                jsonArray = value,
                rootName = key,
                searchQuery = searchQuery,
                path = path,
                activeMatch = activeMatch,
                onFieldClick = onFieldClick,
                onImageClick = onImageClick
            )
        }

        else -> {
            val valueString = value.toString()
            val isString = value is String || value is CharSequence
            val isNumber = value is Number
            val isBoolean = value is Boolean
            val isNull = isJsonNull(value)

            val formattedKey = formatJsonKey(key)
            val formattedValue = formatJsonValue(value)

            val isImage = remember(valueString) {
                isImageUrl(valueString)
            }

            val keyColor = if (key.startsWith("[") && key.endsWith("]")) {
                Color(0xFF808080)
            } else {
                Color(0xFF9CDCFE)
            }

            val valueColor = when {
                isNull -> Color(0xFF808080)
                isString -> {
                    if (isImage) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        Color(0xFFCE9178)
                    }
                }
                isNumber -> Color(0xFFB5CEA8)
                isBoolean -> Color(0xFF569CD6)
                else -> MaterialTheme.colorScheme.onSurface
            }

            val activeKey = activeMatch.occurrenceAt(path, inKey = true)
            val activeValue = activeMatch.occurrenceAt(path, inKey = false)
            val requester = remember { BringIntoViewRequester() }
            LaunchedEffect(activeMatch) {
                if (activeKey >= 0 || activeValue >= 0) requester.bringIntoView()
            }

            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(requester)
                        .clickable(enabled = onFieldClick != null) {
                            onFieldClick?.invoke(path, value)
                        }
                        .padding(vertical = 2.dp)
                ) {
                    Column {
                        HighlightedText(
                            text = formattedKey,
                            query = searchQuery,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = keyColor,
                            activeOccurrence = activeKey
                        )
                        if (isImage) {
                            Icon(
                                Icons.Default.RemoveRedEye,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .size(30.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable {
                                        onImageClick(valueString)
                                    }
                            )
                        }
                    }
                    HighlightedText(
                        text = formattedValue,
                        query = searchQuery,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = valueColor,
                        activeOccurrence = activeValue
                    )
                }
            }
        }
    }
}
