package com.example.devtool.ui.dashboard.network.json

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun JsonNodeViewer(
    key: String,
    value: Any,
    searchQuery: String = "",
    onImageClick: (String) -> Unit
) {

    when (value) {

        is JSONObject -> {
            JsonObjectViewer(
                jsonObject = value,
                rootName = key,
                searchQuery = searchQuery,
                onImageClick = onImageClick
            )
        }

        is JSONArray -> {
            JsonArrayViewer(
                jsonArray = value,
                rootName = key,
                searchQuery = searchQuery,
                onImageClick = onImageClick
            )
        }

        else -> {

            val valueString = value.toString()

            val keyMatches =
                key.contains(searchQuery, ignoreCase = true)

            val valueMatches =
                valueString.contains(searchQuery, ignoreCase = true)

            val isImage = remember(valueString) {
                isImageUrl(valueString)
            }

            if (
                searchQuery.isBlank() ||
                keyMatches ||
                valueMatches
            ) {

                Column {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {

                        Column{
                            HighlightedText(
                                text = "$key: ",
                                query = searchQuery,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if(isImage){
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
                            text = valueString,
                            query = searchQuery,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (isImage)
                                MaterialTheme.colorScheme.secondary
                            else
                                MaterialTheme.colorScheme.onSurface,
                            modifier =

                                    Modifier
                        )


                    }

//                    if (isImage) {
//
//                        AsyncImage(
//                            model = valueString,
//                            contentDescription = null,
//                            modifier = Modifier
//                                .padding(vertical = 4.dp)
//                                .size(60.dp)
//                                .background(
//                                    color = MaterialTheme.colorScheme.surfaceVariant,
//                                    shape = RoundedCornerShape(4.dp)
//                                )
//                                .clickable {
//                                    onImageClick(valueString)
//                                }
//                        )
//                    }
                }
            }
        }
    }
}