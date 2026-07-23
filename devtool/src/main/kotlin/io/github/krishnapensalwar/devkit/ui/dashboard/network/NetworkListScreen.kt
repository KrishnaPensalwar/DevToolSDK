package io.github.krishnapensalwar.devkit.ui.dashboard.network

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.krishnapensalwar.devkit.core.logging.LoggerManager
import io.github.krishnapensalwar.devkit.core.utils.ApiNameExtractor
import io.github.krishnapensalwar.devkit.network.model.NetworkCall
import io.github.krishnapensalwar.devkit.ui.components.DevToolSearchBar
import io.github.krishnapensalwar.devkit.ui.components.StatusDot
import io.github.krishnapensalwar.devkit.ui.dashboard.network.components.NetworkSummaryBadge
import io.github.krishnapensalwar.devkit.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkListScreen(modifier: Modifier = Modifier) {
    val repository = LoggerManager.getNetworkRepository()
    val calls by repository.calls.collectAsState()
    val scope = rememberCoroutineScope()
    var selectedCall by remember { mutableStateOf<NetworkCall?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf<String?>(null) }
    val filters = listOf("ALL", "GET", "POST", "PUT", "DELETE", "PATCH")

    val filteredCalls = calls.filter { call ->
        val matchesQuery = searchQuery.isEmpty() ||
                call.url.contains(searchQuery, ignoreCase = true) ||
                call.endpoint.contains(searchQuery, ignoreCase = true)
        val matchesMethod = selectedMethod == null || selectedMethod == "ALL" ||
                call.method.equals(selectedMethod, ignoreCase = true)
        matchesQuery && matchesMethod
    }

    if (selectedCall != null) {
        NetworkDetailScreen(
            call = selectedCall!!,
            onDismiss = { selectedCall = null },
            modifier = modifier
        )
        return
    }

    Column(modifier = modifier.background(sdkBackground)) {

        NetworkSummaryBadge(calls = calls)

        // Sticky filter header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(sdkBackground)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DevToolSearchBar(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Filter traffic...",
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { scope.launch { repository.clearAll() } },
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(sdkSurface)
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear All",
                        tint = sdkOnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = if (filter == "ALL") selectedMethod == null else selectedMethod == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedMethod = if (filter == "ALL") null else filter
                        },
                        label = { Text(filter, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = sdkSurfaceVariant,
                            selectedLabelColor = sdkPrimary,
                            containerColor = sdkSurface,
                            labelColor = sdkOnSurfaceVariant
                        ),
                        border = null,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        if (filteredCalls.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No network activity", color = sdkOnSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredCalls) { call ->
                    NetworkCallItem(call = call, onClick = { selectedCall = call })
                }
            }
        }
    }
}

@Composable
fun NetworkCallItem(
    call: NetworkCall,
    onClick: () -> Unit
) {
    val statusColor = when {
        call.statusCode in 200..299 -> colorStatusSuccess
        call.statusCode in 300..399 -> colorStatusWarning
        call.statusCode in 400..599 -> colorStatusError
        else -> colorStatusNeutral
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = sdkSurface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = ApiNameExtractor.extract(call.url),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = sdkOnSurface
                )

                Spacer(Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = methodColor(call.method).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = call.method,
                            color = methodColor(call.method),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = call.host,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = sdkOnSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusDot(color = statusColor, modifier = Modifier.size(6.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = call.statusCode.toString(),
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = "${call.duration}ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = sdkOnSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                null,
                tint = sdkSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

fun methodColor(method: String): Color = when (method.uppercase()) {
    "GET"    -> methodGet
    "POST"   -> methodPost
    "PUT"    -> methodPut
    "DELETE" -> methodDelete
    "PATCH"  -> methodPatch
    else     -> colorStatusNeutral
}
