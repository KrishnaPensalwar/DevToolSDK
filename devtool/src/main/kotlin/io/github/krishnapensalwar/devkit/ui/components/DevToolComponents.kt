package io.github.krishnapensalwar.devkit.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.krishnapensalwar.devkit.ui.theme.sdkBackground
import io.github.krishnapensalwar.devkit.ui.theme.sdkOnSurface
import io.github.krishnapensalwar.devkit.ui.theme.sdkOnSurfaceVariant
import io.github.krishnapensalwar.devkit.ui.theme.sdkPrimary
import io.github.krishnapensalwar.devkit.ui.theme.sdkSurface

@Composable
internal fun DevToolTopBar(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = sdkBackground,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = sdkPrimary
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
    }
}

@Composable
internal fun DevToolIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = Color.Transparent,
            contentColor = sdkOnSurfaceVariant
        )
    ) {
        content()
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
internal fun DevToolSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    collapseProgress: Float = 0f,
    onCollapsedClick: (() -> Unit)? = null,
    matchCount: Int? = null,
    currentMatch: Int = 0,
    onPreviousMatch: () -> Unit = {},
    onNextMatch: () -> Unit = {},
) {
    val progress = collapseProgress.coerceIn(0f, 1f)
    val collapsed = progress >= 0.98f
    val fieldAlpha = 1f - progress

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        val barWidth = maxWidth * (1f - progress) + 40.dp * progress
        val barHeight = 48.dp * (1f - progress) + 40.dp * progress
        val horizontalPad = 16.dp * (1f - progress) + 10.dp * progress

        Row(
            modifier = Modifier
                .width(barWidth)
                .height(barHeight)
                .clip(RoundedCornerShape(24.dp))
                .background(sdkSurface)
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .clickable(enabled = collapsed && onCollapsedClick != null) {
                    onCollapsedClick?.invoke()
                }
                .padding(horizontal = horizontalPad),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = if (collapsed) "Search" else null,
                tint = sdkOnSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            if (!collapsed) {
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer { alpha = fieldAlpha }
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 14.sp,
                            color = sdkOnSurfaceVariant
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = sdkOnSurface
                        ),
                        cursorBrush = SolidColor(sdkPrimary),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (value.isNotEmpty() && matchCount != null) {
                    SearchMatchNavigator(
                        matchCount = matchCount,
                        currentMatch = currentMatch,
                        onPrevious = onPreviousMatch,
                        onNext = onNextMatch,
                        modifier = Modifier.graphicsLayer { alpha = fieldAlpha }
                    )
                }
                if (value.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = sdkOnSurfaceVariant,
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer { alpha = fieldAlpha }
                            .clickable { onValueChange("") }
                    )
                }
            }
        }
    }
}

@Composable
internal fun SearchMatchNavigator(
    matchCount: Int,
    currentMatch: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasMatches = matchCount > 0
    val arrowTint = if (hasMatches) sdkOnSurface else sdkOnSurfaceVariant
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = if (hasMatches) "${currentMatch + 1}/$matchCount" else "0/0",
            fontSize = 12.sp,
            color = if (hasMatches) sdkOnSurfaceVariant else MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Icon(
            Icons.Default.KeyboardArrowUp,
            contentDescription = "Previous match",
            tint = arrowTint,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .clickable(enabled = hasMatches, onClick = onPrevious)
                .padding(2.dp)
        )
        Icon(
            Icons.Default.KeyboardArrowDown,
            contentDescription = "Next match",
            tint = arrowTint,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .clickable(enabled = hasMatches, onClick = onNext)
                .padding(2.dp)
        )
    }
}

@Composable
fun StatusDot(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = sdkOnSurfaceVariant,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        modifier = modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun EmptyStateView(
    message: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = sdkOnSurfaceVariant
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = sdkOnSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}