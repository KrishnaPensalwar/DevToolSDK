package com.example.devtool.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.devtool.core.logging.DevLog

@Composable
fun LogList(
    logs: List<DevLog>, 
    modifier: Modifier = Modifier,
    onLogClick: (DevLog) -> Unit = {}
) {
    LazyColumn(modifier = modifier) {
        items(logs, key = { it.id }) { log ->
            LogItem(
                log = log,
                modifier = Modifier.clickable { onLogClick(log) }
            )
        }
    }
}
