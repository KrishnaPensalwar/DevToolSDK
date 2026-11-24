package io.github.krishnapensalwar.devkit.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.krishnapensalwar.devkit.ui.theme.*

@Composable
fun CopyableCard(
    title: String,
    expandedByDefault: Boolean = true,
    onCopy: (() -> Unit)? = null,
    copyLabel: String = title,
    modifier: Modifier = Modifier,
    fillHeight: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    var expanded by rememberSaveable { mutableStateOf(expandedByDefault) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = sdkSurface),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(modifier = if (fillHeight) Modifier.fillMaxSize() else Modifier) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = sdkOnSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        onCopy?.let {
                            it()
                            Toast.makeText(context, "Copied $copyLabel", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = sdkOnSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = sdkOnSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (fillHeight && expanded) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                ) {
                    content()
                }
            } else {
                AnimatedVisibility(expanded) {
                    Box(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp)) {
                        content()
                    }
                }
            }
        }
    }
}
