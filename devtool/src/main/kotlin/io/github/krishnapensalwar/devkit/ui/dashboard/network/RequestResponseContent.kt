package io.github.krishnapensalwar.devkit.ui.dashboard.network

import android.util.Log
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

import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

@Composable
fun RequestContent(
    call: NetworkCall,
    onImageClick: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionLabel(text = "REQUEST")

        CopyableCard(title = "cURL Command", expandedByDefault = false, onCopy = {
            clipboardManager.setText(AnnotatedString(io.github.krishnapensalwar.devkit.network.CurlGenerator.generateCurl(call)))
        }) {
            Text(
                text = io.github.krishnapensalwar.devkit.network.CurlGenerator.generateCurl(call),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }

        CopyableCard(title = "HEADERS", expandedByDefault = false,) {
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
    Log.d("Network", "RequestContent: $call")


    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionLabel(text = "RESPONSE")

        CopyableCard(title = "HEADERS", expandedByDefault = false, ) {
            SearchableHeaderList(headers = call.responseHeaders)
        }

        CopyableCard(title = "BODY", onCopy = {
            clipboardManager.setText(AnnotatedString(call.responseBody ?: ""))
        }) {
            SearchableBody(body = call.responseBody, onImageClick = onImageClick)
        }
    }
}