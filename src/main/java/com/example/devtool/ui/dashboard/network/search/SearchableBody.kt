package com.example.devtool.ui.dashboard.network.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.devtool.ui.components.DevToolSearchBar
import com.example.devtool.ui.dashboard.network.json.ExpandableJsonViewer
import com.example.devtool.ui.dashboard.network.json.extractImageUrls

@Composable
fun SearchableBody(
    body: String?,
    onImageClick: (String) -> Unit
) {

    var searchQuery by remember {
        mutableStateOf("")
    }

    val imageUrls = remember(body) {
        extractImageUrls(body)
    }

    Column {

        if (imageUrls.isNotEmpty()) {

            Text(
                text = "Detected Images",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                imageUrls.forEach { url ->

                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onImageClick(url)
                            }
                    )
                }
            }
        }

        DevToolSearchBar(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
            },
            placeholder = "Search body...",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        ExpandableJsonViewer(
            jsonString = body,
            searchQuery = searchQuery,
            onImageClick = onImageClick
        )
    }
}