package io.github.krishnapensalwar.devkit.ui.dashboard.network.components

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import io.github.krishnapensalwar.devkit.DevToolSdk
import io.github.krishnapensalwar.devkit.cache.CacheManager
import io.github.krishnapensalwar.devkit.mock.identity.RequestIdentity
import io.github.krishnapensalwar.devkit.mock.scenario.MockScenarioRepository
import io.github.krishnapensalwar.devkit.network.model.NetworkCall
import io.github.krishnapensalwar.devkit.network.model.mockScenarioName
import io.github.krishnapensalwar.devkit.ui.navigation.Destination
import io.github.krishnapensalwar.devkit.ui.navigation.navigateTo
import io.github.krishnapensalwar.devkit.ui.theme.sdkOnSurface
import io.github.krishnapensalwar.devkit.ui.theme.sdkOnSurfaceVariant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Composable
fun SourceCard(
    isMocked: Boolean,
    scenarioSelected: Boolean,
    cacheSelected: Boolean,
    mockSource: String?,
    activeName: String?,
    activeStatus: Int?,
    call: NetworkCall,
    navController: NavController,
    scope: CoroutineScope,
    identity: RequestIdentity,
    context: Context
) {
    val isLocalResponse = isMocked
    val status = call.statusCode.takeIf { it != 0 } ?: 200
    val body = call.responseBody.orEmpty()
    val headersJson = JSONObject(call.responseHeaders).toString()

    val nextRequestLabel = when {
        scenarioSelected && activeName != null -> "Next request: $activeName"
        cacheSelected -> "Next request: Cached response"
        else -> null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocalResponse) {
                Color(0xFF13261C)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SourceBadge(isLocalResponse)
                Text(
                    text = "Status: ${call.statusCode.takeIf { it > 0 } ?: "—"}",
                    fontSize = 12.sp,
                    color = sdkOnSurfaceVariant
                )
            }

            mockSource?.let {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "via $it",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Information + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    if (isMocked) {
                        Text(
                            text = "Served by mock: ${call.mockScenarioName ?: mockSource ?: "DevTool"}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = sdkOnSurface,
                            maxLines = 1
                        )
                    } else {
                        Text(
                            text = "Response came from the network server.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                    if (nextRequestLabel != null) {
                        Text(
                            text = nextRequestLabel + (activeStatus?.let { " · $it" } ?: ""),
                            fontSize = 11.sp,
                            color = Color(0xFF4ADE80),
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                ActionIconButton(
                    icon = Icons.AutoMirrored.Filled.AltRoute,
                    description = "Scenarios",
                    onClick = {
                        navController.navigateTo(
                            Destination.MockScenarios(
                                url = call.url,
                                method = call.method,
                                initialBody = body,
                                initialStatus = status,
                                headersJson = headersJson,
                                requestBody = call.requestBody.orEmpty()
                            )
                        )
                    }
                )

                Spacer(modifier = Modifier.width(4.dp))

                ActionIconButton(
                    icon = Icons.Default.Replay,
                    description = "Override",
                    onClick = {
                        saveOverride(
                            scope = scope,
                            context = context,
                            identity = identity,
                            status = status,
                            headersJson = headersJson,
                            body = body
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SourceBadge(
    isLocalResponse: Boolean
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isLocalResponse) {
                    Color(0xFF4ADE80)
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
            .padding(
                horizontal = 8.dp,
                vertical = 4.dp
            )
    ) {
        Text(
            text = if (isLocalResponse) {
                "MOCKED"
            } else {
                "LIVE SERVER"
            },
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isLocalResponse) {
                Color.Black
            } else {
                MaterialTheme.colorScheme.onPrimary
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(description)
            }
        },
        state = rememberTooltipState()
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = description,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun saveOverride(
    scope: CoroutineScope,
    context: Context,
    identity: RequestIdentity,
    status: Int,
    headersJson: String,
    body: String
) {
    scope.launch(Dispatchers.IO) {
        try {
            MockScenarioRepository.saveOverride(
                identity = identity,
                status = status,
                headersJson = headersJson,
                body = body
            )

            CacheManager.saveWithHeadersJson(
                identity = identity,
                status = status,
                headersJson = headersJson,
                body = body
            )

            withContext(Dispatchers.Main) {
                val message =
                    if (DevToolSdk.isMockingEnabled()) {
                        "Override saved. Next request uses this body."
                    } else {
                        "Override saved. Turn on Mock Network Traffic to apply."
                    }

                Toast.makeText(
                    context,
                    message,
                    Toast.LENGTH_SHORT
                ).show()
            }

        } catch (e: Exception) {
            Log.e(
                "NetworkInterceptor",
                "Error overriding response",
                e
            )

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Failed to save override: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}