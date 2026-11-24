package io.github.krishnapensalwar.devkit.ui.dashboard.network

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import io.github.krishnapensalwar.devkit.core.logging.LoggerManager
import io.github.krishnapensalwar.devkit.mock.identity.RequestIdentity
import io.github.krishnapensalwar.devkit.network.model.NetworkCall
import io.github.krishnapensalwar.devkit.network.model.isMocked
import io.github.krishnapensalwar.devkit.network.model.matchesQuery
import io.github.krishnapensalwar.devkit.ui.dashboard.components.TopSection
import io.github.krishnapensalwar.devkit.ui.navigation.Destination
import io.github.krishnapensalwar.devkit.ui.navigation.navigateTo
import io.github.krishnapensalwar.devkit.ui.theme.colorStatusError
import io.github.krishnapensalwar.devkit.ui.theme.colorStatusNeutral
import io.github.krishnapensalwar.devkit.ui.theme.colorStatusSuccess
import io.github.krishnapensalwar.devkit.ui.theme.colorStatusWarning
import io.github.krishnapensalwar.devkit.ui.theme.methodDelete
import io.github.krishnapensalwar.devkit.ui.theme.methodGet
import io.github.krishnapensalwar.devkit.ui.theme.methodPatch
import io.github.krishnapensalwar.devkit.ui.theme.methodPost
import io.github.krishnapensalwar.devkit.ui.theme.methodPut
import io.github.krishnapensalwar.devkit.ui.theme.sdkBackground
import io.github.krishnapensalwar.devkit.ui.theme.sdkOnSurface
import io.github.krishnapensalwar.devkit.ui.theme.sdkOnSurfaceVariant
import io.github.krishnapensalwar.devkit.ui.theme.sdkSurface
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NetworkListScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val repository = LoggerManager.getNetworkRepository()
    val calls by repository.calls.collectAsState()
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedMethods by remember { mutableStateOf(setOf<String>()) }
    var selectedStatuses by remember { mutableStateOf(setOf<String>()) }
    var selectedTypes by remember { mutableStateOf(setOf<String>()) }

    val existingMethods = remember(calls) {
        val methods = calls.map { it.method.uppercase() }.distinct()
        if (methods.isEmpty()) listOf("GET", "POST", "PUT", "DELETE", "PATCH") else methods.sorted()
    }

    val filteredCalls = calls.filter { call ->
        val identity = RequestIdentity.parse(call.url, call.method, call.requestBody)
        val matchesQuery = searchQuery.isEmpty() ||
                call.matchesQuery(searchQuery, identity)
        val matchesMethod = selectedMethods.isEmpty() ||
                selectedMethods.contains(call.method.uppercase())
        val matchesStatus = selectedStatuses.isEmpty() || selectedStatuses.any { status ->
            when (status) {
                "Success" -> call.statusCode in 200..299
                "Failed" -> call.statusCode in 400..599 || call.exception != null
                "Pending" -> call.statusCode <= 0 && call.exception == null
                else -> true
            }
        }
        val matchesType = selectedTypes.isEmpty() || selectedTypes.any { type ->
            when (type) {
                "GraphQL" -> identity.isGraphQl
                "REST" -> !identity.isGraphQl
                else -> true
            }
        }
        matchesQuery && matchesMethod && matchesStatus && matchesType
    }

    Column(modifier = modifier.background(sdkBackground)) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TopSection(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            selectedMethods = selectedMethods,
            onMethodsChange = { selectedMethods = it },
            existingMethods = existingMethods,
            selectedStatuses = selectedStatuses,
            onStatusesChange = { selectedStatuses = it },
            selectedTypes = selectedTypes,
            onTypesChange = { selectedTypes = it },
            onClearConfirmed = { scope.launch { repository.clearAll() } }
        )
    }

    if (filteredCalls.isEmpty()) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("No network activity", color = sdkOnSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(filteredCalls, key = { it.id }) { call ->
                NetworkCallItem(
                    call = call,
                    onClick = { navController.navigateTo(Destination.NetworkDetail(call.id, searchQuery.trim())) }
                )
            }
        }
    }
    }
}

@Composable
internal fun NetworkCallItem(
    call: NetworkCall,
    onClick: () -> Unit,
    identity: RequestIdentity = RequestIdentity.parse(call.url, call.method, call.requestBody)
) {
    val statusColor = when {
        call.statusCode in 200..299 -> colorStatusSuccess
        call.statusCode in 300..399 -> colorStatusWarning
        call.statusCode in 400..599 -> colorStatusError
        else -> colorStatusNeutral
    }

    val label = identity.graphQlOperationName?.takeIf { it.isNotBlank() }
        ?: identity.endpoint.ifBlank { call.endpoint }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = sdkSurface)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = call.method,
                color = methodColor(call.method),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                modifier = Modifier.width(64.dp)
            )
            Text(
                text = label,
                color = sdkOnSurface,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (call.isMocked) {
                Text(
                    text = "MOCK",
                    color = Color.Black,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .background(colorStatusSuccess, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Text(
                text = call.statusCode.toString(),
                color = statusColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = sdkOnSurfaceVariant,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(18.dp)
            )
        }
    }
}

internal fun methodColor(method: String): Color = when (method.uppercase()) {
    "GET" -> methodGet
    "POST" -> methodPost
    "PUT" -> methodPut
    "DELETE" -> methodDelete
    "PATCH" -> methodPatch
    else -> colorStatusNeutral
}
