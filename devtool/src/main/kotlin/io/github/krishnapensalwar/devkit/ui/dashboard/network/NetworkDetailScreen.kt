package io.github.krishnapensalwar.devkit.ui.dashboard.network

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import io.github.krishnapensalwar.devkit.mock.identity.RequestIdentity
import io.github.krishnapensalwar.devkit.mock.scenario.MockApiUiState
import io.github.krishnapensalwar.devkit.mock.scenario.MockScenarioCatalog
import io.github.krishnapensalwar.devkit.mock.scenario.MockScenarioRepository
import io.github.krishnapensalwar.devkit.network.model.NetworkCall
import io.github.krishnapensalwar.devkit.network.model.isMocked
import io.github.krishnapensalwar.devkit.network.model.mockScenarioName
import io.github.krishnapensalwar.devkit.network.model.mockSource
import io.github.krishnapensalwar.devkit.network.model.responseBodyContains
import io.github.krishnapensalwar.devkit.network.model.responseHeadersContain
import io.github.krishnapensalwar.devkit.ui.dashboard.network.components.SourceCard
import io.github.krishnapensalwar.devkit.ui.navigation.pop
import io.github.krishnapensalwar.devkit.ui.theme.sdkBackground
import io.github.krishnapensalwar.devkit.ui.theme.sdkOnSurface
import io.github.krishnapensalwar.devkit.ui.theme.sdkOnSurfaceVariant
import io.github.krishnapensalwar.devkit.ui.theme.sdkSurface
import io.github.krishnapensalwar.devkit.ui.theme.sdkSurfaceVariant

internal enum class DetailTab { OVERVIEW, REQUEST, RESPONSE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NetworkDetailScreen(
    call: NetworkCall,
    navController: NavController,
    modifier: Modifier = Modifier,
    initialSearchQuery: String = ""
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val bodyQuery = initialSearchQuery.takeIf { call.responseBodyContains(it) }.orEmpty()
    val headersQuery = initialSearchQuery
        .takeIf { bodyQuery.isEmpty() && call.responseHeadersContain(it) }.orEmpty()
    var selectedTab by remember {
        mutableStateOf(
            if (bodyQuery.isNotEmpty() || headersQuery.isNotEmpty()) DetailTab.RESPONSE else DetailTab.OVERVIEW
        )
    }
    var fullImageUrl by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val mockSource = call.mockSource
    val mockScenarioHeader = call.mockScenarioName
    val isMocked = call.isMocked

    val identity = remember(call.url, call.method, call.requestBody) {
        RequestIdentity.parse(call.url, call.method, call.requestBody)
    }
    val mockState by MockScenarioRepository.observe(identity)
        .collectAsState(initial = MockApiUiState())
    val globalMockOn by io.github.krishnapensalwar.devkit.DevToolSdk.mockingEnabledFlow.collectAsState()
    val endpointMockOn = globalMockOn && mockState.enabled
    val scenarioSelected =
        endpointMockOn && !mockState.useCachedBody && mockState.activeScenarioKey != null
    val cacheSelected = endpointMockOn && mockState.useCachedBody
    val activeBuiltIn = MockScenarioCatalog.findByKey(mockState.activeScenarioKey.orEmpty())
    val activeCustom = mockState.customScenarios.find { it.key == mockState.activeScenarioKey }
    val activeName = when {
        cacheSelected -> "Cached response"
        scenarioSelected -> activeBuiltIn?.title ?: activeCustom?.name ?: mockScenarioHeader
        else -> mockScenarioHeader
    }
    val activeStatus = when {
        cacheSelected -> call.statusCode.takeIf { it > 0 }
        scenarioSelected -> activeBuiltIn?.statusCode ?: activeCustom?.statusCode
        ?: call.statusCode.takeIf { it > 0 }

        else -> null
    }

    Column(
        modifier = modifier.background(sdkBackground)
    ) {
        Column(
            modifier = Modifier
                .background(sdkBackground)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { navController.pop() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(sdkSurface)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = sdkOnSurface
                    )
                }
                Text(
                    text = "Traffic Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = sdkOnSurface
                )
                Row {
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(formatFullLog(call)))
                            Toast.makeText(context, "Copied full log", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(sdkSurface)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = sdkOnSurface
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(sdkSurface)
                    .padding(4.dp)
            ) {
                DetailTab.entries.forEach { tab ->
                    val selected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) sdkSurfaceVariant else Color.Transparent)
                            .clickable { selectedTab = tab }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                            fontSize = 14.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) sdkOnSurface else sdkOnSurfaceVariant
                        )
                    }
                }
            }
        }
        if (selectedTab == DetailTab.RESPONSE) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                ResponseContent(
                    call,
                    onImageClick = { fullImageUrl = it },
                    initialBodyQuery = bodyQuery,
                    initialHeadersQuery = headersQuery
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    DetailTab.OVERVIEW -> {
                        SourceCard(
                            isMocked,
                            scenarioSelected,
                            cacheSelected,
                            mockSource,
                            activeName,
                            activeStatus,
                            call,
                            navController,
                            scope,
                            identity,
                            context
                        )
                        OverviewContent(call)
                    }
                    DetailTab.REQUEST -> RequestContent(call, onImageClick = { fullImageUrl = it })
                    DetailTab.RESPONSE -> Unit
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    fullImageUrl?.let { image ->
        Dialog(onDismissRequest = { fullImageUrl = null }) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.9f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { fullImageUrl = null }) {
                    AsyncImage(
                        model = image,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                    )
                    IconButton(
                        onClick = { fullImageUrl = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
        }
    }
}

internal fun formatFullLog(call: NetworkCall): String = buildString {
    appendLine("URL: ${call.url}")
    appendLine("Method: ${call.method}")
    appendLine("Status: ${call.statusCode}")
    appendLine("Duration: ${call.duration}ms")
    appendLine("\n--- Request Headers ---")
    call.requestHeaders.forEach { appendLine("${it.key}: ${it.value}") }
    appendLine("\n--- Request Body ---")
    appendLine(call.requestBody ?: "None")
    appendLine("\n--- Response Headers ---")
    call.responseHeaders.forEach { appendLine("${it.key}: ${it.value}") }
    appendLine("\n--- Response Body ---")
    appendLine(call.responseBody ?: "None")
}
