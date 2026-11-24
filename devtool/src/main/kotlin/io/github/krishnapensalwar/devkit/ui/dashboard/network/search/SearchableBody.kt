package io.github.krishnapensalwar.devkit.ui.dashboard.network.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.krishnapensalwar.devkit.ui.components.DevToolSearchBar
import io.github.krishnapensalwar.devkit.ui.dashboard.network.json.ExpandableJsonViewer
import io.github.krishnapensalwar.devkit.ui.dashboard.network.json.rememberJsonSearch

@Composable
fun SearchableBody(
    body: String?,
    onImageClick: (String) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    val search = rememberJsonSearch(body, searchQuery)

    Column {
        DevToolSearchBar(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = "Search body...",
            matchCount = search.matches.size,
            currentMatch = search.index,
            onPreviousMatch = search::previous,
            onNextMatch = search::next,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        ExpandableJsonViewer(
            jsonString = body,
            searchQuery = searchQuery,
            activeMatch = search.active,
            onImageClick = onImageClick
        )
    }
}
