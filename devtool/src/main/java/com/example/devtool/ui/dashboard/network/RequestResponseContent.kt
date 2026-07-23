package com.example.devtool.ui.dashboard.network.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.devtool.network.model.NetworkCall
import com.example.devtool.ui.components.CopyableCard
import com.example.devtool.ui.components.SectionLabel
import com.example.devtool.ui.dashboard.network.search.SearchableBody
import com.example.devtool.ui.dashboard.network.search.SearchableHeaderList
import com.example.devtool.ui.theme.*

@Composable
fun RequestContent(
    call: NetworkCall,
    onImageClick: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionLabel(text = "REQUEST")

        CopyableCard(title = "HEADERS", onCopy = {
            clipboardManager.setText(AnnotatedString(formatMap(call.requestHeaders)))
        }) {
            SearchableHeaderList(headers = call.requestHeaders)
        }

        CopyableCard(title = "BODY", onCopy = {
            clipboardManager.setText(AnnotatedString(call.requestBody ?: ""))
        }) {
            SearchableBody(body = call.requestBody, onImageClick = onImageClick)
        }
    }
}

@Composable
fun ResponseContent(
    call: NetworkCall,
    onImageClick: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionLabel(text = "RESPONSE")

        CopyableCard(title = "HEADERS", expandedByDefault = false, onCopy = {
            clipboardManager.setText(AnnotatedString(formatMap(call.responseHeaders)))
        }) {
            SearchableHeaderList(headers = call.responseHeaders)
        }

        CopyableCard(title = "BODY", onCopy = {
            clipboardManager.setText(AnnotatedString(call.responseBody ?: ""))
        }) {
            SearchableBody(body = call.responseBody, onImageClick = onImageClick)
        }
    }
}

private fun formatMap(map: Map<String, String>): String = buildString {
    map.forEach { (k, v) -> appendLine("$k: $v") }
}
