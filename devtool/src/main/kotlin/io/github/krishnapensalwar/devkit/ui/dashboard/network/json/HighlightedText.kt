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

private val matchStyle = SpanStyle(background = Color(0xFF854D0E), color = Color(0xFFFDE68A))
private val activeMatchStyle = SpanStyle(background = Color(0xFFF59E0B), color = Color.Black)

@Composable
fun HighlightedText(
    text: String,
    query: String,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
    fontSize: TextUnit = 14.sp,
    fontFamily: FontFamily? = null,
    color: Color = MaterialTheme.colorScheme.onSurface,
    activeOccurrence: Int = -1
) {
    val hits = if (query.isBlank()) emptyList() else occurrencesOf(text, query)

    if (hits.isEmpty()) {
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
        hits.forEachIndexed { i, start ->
            append(text.substring(current, start))
            withStyle(if (i == activeOccurrence) activeMatchStyle else matchStyle) {
                append(text.substring(start, start + query.length))
            }
            current = start + query.length
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
