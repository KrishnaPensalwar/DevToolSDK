package io.github.krishnapensalwar.devkit.ui.dashboard.network.json

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpandableJsonViewer(
    jsonString: String?,
    searchQuery: String = "",
    path: List<String> = emptyList(),
    activeMatch: JsonMatch? = null,
    onFieldClick: ((List<String>, Any) -> Unit)? = null,
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

    when (val parsed = remember(jsonString) { parseJson(jsonString) }) {
        is JSONObject -> {
            JsonObjectViewer(
                jsonObject = parsed,
                rootName = "Root",
                searchQuery = searchQuery,
                path = path,
                activeMatch = activeMatch,
                onFieldClick = onFieldClick,
                onImageClick = onImageClick
            )
        }

        is JSONArray -> {
            JsonArrayViewer(
                jsonArray = parsed,
                rootName = "Root",
                searchQuery = searchQuery,
                path = path,
                activeMatch = activeMatch,
                onFieldClick = onFieldClick,
                onImageClick = onImageClick
            )
        }

        else -> {
            val active = activeMatch.occurrenceAt(path, inKey = false)
            val requester = remember { BringIntoViewRequester() }
            LaunchedEffect(activeMatch) { if (active >= 0) requester.bringIntoView() }
            HighlightedText(
                text = jsonString,
                query = searchQuery,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                activeOccurrence = active,
                modifier = Modifier.bringIntoViewRequester(requester)
            )
        }
    }
}
