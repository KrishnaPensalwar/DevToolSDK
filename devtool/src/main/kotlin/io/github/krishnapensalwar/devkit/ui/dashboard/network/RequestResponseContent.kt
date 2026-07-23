package io.github.krishnapensalwar.devkit.ui.dashboard.network

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import io.github.krishnapensalwar.devkit.network.model.NetworkCall
import io.github.krishnapensalwar.devkit.ui.components.CopyableCard
import io.github.krishnapensalwar.devkit.ui.components.SectionLabel
import io.github.krishnapensalwar.devkit.ui.dashboard.network.search.SearchableBody
import io.github.krishnapensalwar.devkit.ui.dashboard.network.search.SearchableHeaderList

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