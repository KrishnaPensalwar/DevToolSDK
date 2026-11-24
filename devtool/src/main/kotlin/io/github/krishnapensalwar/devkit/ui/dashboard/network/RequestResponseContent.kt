package io.github.krishnapensalwar.devkit.ui.dashboard.network

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.krishnapensalwar.devkit.network.model.NetworkCall
import io.github.krishnapensalwar.devkit.ui.components.CopyableCard
import io.github.krishnapensalwar.devkit.ui.components.DevToolSearchBar
import io.github.krishnapensalwar.devkit.ui.components.SearchMatchNavigator
import io.github.krishnapensalwar.devkit.ui.components.SectionLabel
import io.github.krishnapensalwar.devkit.ui.dashboard.network.json.ExpandableJsonViewer
import io.github.krishnapensalwar.devkit.ui.dashboard.network.json.rememberJsonSearch
import io.github.krishnapensalwar.devkit.ui.dashboard.network.search.SearchableBody
import io.github.krishnapensalwar.devkit.ui.dashboard.network.search.SearchableHeaderList
import io.github.krishnapensalwar.devkit.ui.theme.sdkOnSurface
import io.github.krishnapensalwar.devkit.ui.theme.sdkOnSurfaceVariant
import io.github.krishnapensalwar.devkit.ui.theme.sdkSurface
import io.github.krishnapensalwar.devkit.ui.theme.sdkSurfaceVariant
import kotlinx.coroutines.launch

@Composable
internal fun RequestContent(
    call: NetworkCall,
    onImageClick: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionLabel(text = "REQUEST")

        CopyableCard(title = "cURL Command", copyLabel = "cURL", expandedByDefault = false, onCopy = {
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

        CopyableCard(title = "BODY", copyLabel = "request body", expandedByDefault = true, onCopy = {
            clipboardManager.setText(AnnotatedString(call.requestBody ?: ""))
        }) {
            SearchableBody(body = call.requestBody, onImageClick = onImageClick)
        }
    }
}

@Composable
internal fun ResponseContent(
    call: NetworkCall,
    onImageClick: (String) -> Unit,
    initialBodyQuery: String = "",
    initialHeadersQuery: String = ""
) {
    val clipboardManager = LocalClipboardManager.current
    var headersExpanded by rememberSaveable { mutableStateOf(initialHeadersQuery.isNotEmpty()) }
    var searchQuery by remember { mutableStateOf(initialBodyQuery) }
    val search = rememberJsonSearch(call.responseBody, searchQuery)

    val density = LocalDensity.current
    val collapseRangePx = with(density) { 48.dp.toPx() }
    var collapseOffsetPx by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    val collapseProgress = (collapseOffsetPx / collapseRangePx).coerceIn(0f, 1f)

    val connection = remember(collapseRangePx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.Drag) return Offset.Zero
                val old = collapseOffsetPx
                val updated = (old - available.y).coerceIn(0f, collapseRangePx)
                collapseOffsetPx = updated
                return Offset(0f, -(updated - old))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(connection)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionLabel(text = "RESPONSE", modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(sdkSurface)
                    .clickable { headersExpanded = !headersExpanded }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Headers (${call.responseHeaders.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = sdkOnSurface
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = if (headersExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (headersExpanded) "Hide headers" else "Show headers",
                    tint = sdkOnSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        AnimatedVisibility(visible = headersExpanded) {
            Card(
                colors = CardDefaults.cardColors(containerColor = sdkSurface),
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier

                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    SearchableHeaderList(headers = call.responseHeaders, initialQuery = initialHeadersQuery)
                }
            }
        }

        DevToolSearchBar(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = "Search body...",
            collapseProgress = collapseProgress,
            onCollapsedClick = {
                scope.launch {
                    animate(
                        initialValue = collapseOffsetPx,
                        targetValue = 0f,
                        animationSpec = tween(220)
                    ) { value, _ -> collapseOffsetPx = value }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp * (1f - collapseProgress) + 4.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            CopyableCard(
                title = "BODY",
                copyLabel = "response body",
                expandedByDefault = true,
                fillHeight = true,
                modifier = Modifier.fillMaxSize(),
                onCopy = {
                    clipboardManager.setText(AnnotatedString(call.responseBody ?: ""))
                }
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    ExpandableJsonViewer(
                        jsonString = call.responseBody,
                        searchQuery = searchQuery,
                        activeMatch = search.active,
                        onImageClick = onImageClick
                    )
                }
            }

            if (searchQuery.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = sdkSurfaceVariant,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    SearchMatchNavigator(
                        matchCount = search.matches.size,
                        currentMatch = search.index,
                        onPrevious = search::previous,
                        onNext = search::next,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
