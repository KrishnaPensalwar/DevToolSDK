package io.github.krishnapensalwar.devkit.ui.dashboard.network.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.krishnapensalwar.devkit.ui.components.DevToolSearchBar
import io.github.krishnapensalwar.devkit.ui.dashboard.network.json.HighlightedText

@Composable
fun SearchableHeaderList(
    headers: Map<String, String>
) {

    var searchQuery by remember {
        mutableStateOf("")
    }

    val filteredHeaders = remember(headers, searchQuery) {

        if (searchQuery.isBlank()) {
            headers
        } else {
            headers.filter {
                it.key.contains(searchQuery, true) ||
                        it.value.contains(searchQuery, true)
            }
        }
    }

    Column {

        DevToolSearchBar(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
            },
            placeholder = "Search headers...",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        if (filteredHeaders.isEmpty()) {

            Text(
                text = "No matching headers",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )

        } else {

            filteredHeaders.forEach { (key, value) ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {

                    HighlightedText(
                        text = key,
                        query = searchQuery,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(0.4f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    HighlightedText(
                        text = value,
                        query = searchQuery,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(0.6f)
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}