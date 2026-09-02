package io.github.krishnapensalwar.devkit.ui.dashboard.network

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import io.github.krishnapensalwar.devkit.mock.identity.RequestIdentity
import io.github.krishnapensalwar.devkit.mock.scenario.MockApiUiState
import io.github.krishnapensalwar.devkit.mock.scenario.MockScenarioCatalog
import io.github.krishnapensalwar.devkit.mock.scenario.MockScenarioRepository
import io.github.krishnapensalwar.devkit.network.model.NetworkCall
import androidx.navigation.NavController
import io.github.krishnapensalwar.devkit.ui.navigation.Destination
import io.github.krishnapensalwar.devkit.ui.navigation.navigateTo
import io.github.krishnapensalwar.devkit.ui.navigation.pop
import io.github.krishnapensalwar.devkit.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal enum class DetailTab { OVERVIEW, REQUEST, RESPONSE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NetworkDetailScreen(
    call: NetworkCall,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(DetailTab.OVERVIEW) }
    var fullImageUrl by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val mockSource = call.responseHeaders.entries.find { it.key.equals("X-Mock-Source", ignoreCase = true) }?.value
    val mockScenarioHeader = call.responseHeaders.entries.find { it.key.equals("X-Mock-Scenario", ignoreCase = true) }?.value
    val isMocked = mockSource != null || call.exception?.startsWith("DevTool mock") == true ||
        (call.exception?.contains("DevTool mock") == true)

    val identity = remember(call.url, call.method, call.requestBody) {
        RequestIdentity.parse(call.url, call.method, call.requestBody)
    }
    val mockState by MockScenarioRepository.observe(identity)
        .collectAsState(initial = MockApiUiState())
    val activeBuiltIn = MockScenarioCatalog.findByKey(mockState.activeScenarioKey.orEmpty())
    val activeCustom = mockState.customScenarios.find { it.key == mockState.activeScenarioKey }
    val activeName = activeBuiltIn?.title ?: activeCustom?.name ?: mockScenarioHeader
    val activeStatus = activeBuiltIn?.statusCode ?: activeCustom?.statusCode ?: call.statusCode.takeIf { it > 0 }

    Scaffold(
        modifier = modifier,
        containerColor = sdkBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .background(sdkBackground)
                    .statusBarsPadding()
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
//                        IconButton(
//                            onClick = { /* Refresh */ },
//                            modifier = Modifier
//                                .size(44.dp)
//                                .clip(CircleShape)
//                                .background(sdkSurface)
//                        ) {
//                            Icon(
//                                Icons.Default.Refresh,
//                                contentDescription = "Refresh",
//                                tint = sdkOnSurface
//                            )
//                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(formatFullLog(call)))
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
                                .padding(vertical = 10.dp),
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMocked || mockState.activeScenarioKey != null)
                        Color(0xFF13261C) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isMocked || mockState.activeScenarioKey != null)
                                                Color(0xFF4ADE80) else MaterialTheme.colorScheme.outline
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isMocked || mockState.activeScenarioKey != null) "MOCKED" else "LIVE SERVER",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isMocked || mockState.activeScenarioKey != null) Color.Black else MaterialTheme.colorScheme.surface
                                    )
                                }
                                if (mockSource != null) {
                                    Text(
                                        text = "via $mockSource",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            if (activeName != null) {
                                Text(
                                    text = "Scenario: $activeName",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = sdkOnSurface
                                )
                                Text(
                                    text = "Status: ${activeStatus ?: "—"}",
                                    fontSize = 12.sp,
                                    color = sdkOnSurfaceVariant
                                )
                            } else {
                                Text(
                                    text = if (isMocked) "This response was served locally from database." else "This response came directly from the network server.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val headersJson = org.json.JSONObject(call.responseHeaders).toString()
                                navController.navigateTo(
                                    Destination.MockScenarios(
                                        url = call.url,
                                        method = call.method,
                                        initialBody = call.responseBody.orEmpty(),
                                        initialStatus = if (call.statusCode != 0) call.statusCode else 200,
                                        headersJson = headersJson,
                                        requestBody = call.requestBody.orEmpty()
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4ADE80), contentColor = Color.Black),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Scenarios", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        val headersJson = org.json.JSONObject(call.responseHeaders).toString()
                                        io.github.krishnapensalwar.devkit.cache.CacheManager.saveWithHeadersJson(
                                            url = call.url,
                                            method = call.method,
                                            status = if (call.statusCode != 0) call.statusCode else 200,
                                            headersJson = headersJson,
                                            body = call.responseBody ?: ""
                                        )
                                        Log.d("NetworkInterceptor", "[NetworkDetailScreen] Saved mock response override for ${call.url}")
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Response override saved successfully", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Log.e("NetworkInterceptor", "[NetworkDetailScreen] Error overriding response: ${e.message}")
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Failed to save override: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Override",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            when (selectedTab) {
                DetailTab.OVERVIEW -> OverviewContent(call)
                DetailTab.REQUEST -> RequestContent(call, onImageClick = { fullImageUrl = it })
                DetailTab.RESPONSE -> ResponseContent(call, onImageClick = { fullImageUrl = it })
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    fullImageUrl?.let { image ->
        Dialog(onDismissRequest = { fullImageUrl = null }) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.9f)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().clickable { fullImageUrl = null }) {
                    AsyncImage(
                        model = image,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    )
                    IconButton(
                        onClick = { fullImageUrl = null },
                        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
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
