package io.github.krishnapensalwar.devkit.ui.dashboard.network.json


import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun HighlightedText(
    text: String,
    query: String,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
    fontSize: TextUnit = 14.sp,
    fontFamily: FontFamily? = null,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    if (query.isBlank()) {
        Text(
            text = text,
            modifier = modifier,
            fontWeight = fontWeight,
            fontSize = fontSize,
            fontFamily = fontFamily,
            color = color
        )
        return
    }

    val startIndex = text.indexOf(query, ignoreCase = true)

    if (startIndex == -1) {
        Text(
            text = text,
            modifier = modifier,
            fontWeight = fontWeight,
            fontSize = fontSize,
            fontFamily = fontFamily,
            color = color
        )
        return
    }

    val annotated = buildAnnotatedString {
        var current = 0
        var match = text.indexOf(query, current, ignoreCase = true)

        while (match >= 0) {
            append(text.substring(current, match))

            withStyle(
                SpanStyle(
                    background = Color(0xFF854D0E),
                    color = Color(0xFFFDE68A)
                )
            ) {
                append(text.substring(match, match + query.length))
            }

            current = match + query.length
            match = text.indexOf(query, current, ignoreCase = true)
        }

        append(text.substring(current))
    }

    Text(
        text = annotated,
        modifier = modifier,
        fontWeight = fontWeight,
        fontSize = fontSize,
        fontFamily = fontFamily,
        color = color
    )
}