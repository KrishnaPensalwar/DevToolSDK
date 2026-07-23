package io.github.krishnapensalwar.devkit.ui.dashboard.network.json

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun ExpandableJsonViewer(
    jsonString: String?,
    searchQuery: String = "",
    onImageClick: (String) -> Unit
) {
    if (jsonString.isNullOrBlank()) {
        Text(
            text = "No body",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val jsonObject = try {
        JSONObject(jsonString)
    } catch (_: Exception) {
        null
    }

    val jsonArray = try {
        if (jsonObject == null) JSONArray(jsonString) else null
    } catch (_: Exception) {
        null
    }

    when {
        jsonObject != null -> {
            JsonObjectViewer(
                jsonObject = jsonObject,
                rootName = "Root",
                searchQuery = searchQuery,
                onImageClick = onImageClick
            )
        }

        jsonArray != null -> {
            JsonArrayViewer(
                jsonArray = jsonArray,
                rootName = "Root",
                searchQuery = searchQuery,
                onImageClick = onImageClick
            )
        }

        else -> {
            HighlightedText(
                text = jsonString,
                query = searchQuery,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}