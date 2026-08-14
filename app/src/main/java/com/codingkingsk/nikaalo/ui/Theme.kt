package com.codingkingsk.nikaalo.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Ink = Color(0xFF0B1015)
val InkSoft = Color(0xFF141C24)
val Card = Color(0xFF1B242E)
val Asphalt = Color(0xFF3B424B)
val Wall = Color(0xFF161C23)
val Lane = Color(0xFFE9EDF2)
val Accent = Color(0xFFFFC53D)
val AccentDeep = Color(0xFFE8A200)
val Success = Color(0xFF35C759)
val TextPrimary = Color(0xFFF4F7FA)
val TextMuted = Color(0xFF93A1B1)

private val NikaaloColors = darkColorScheme(
    primary = Accent,
    onPrimary = Ink,
    secondary = Success,
    onSecondary = Ink,
    background = Ink,
    onBackground = TextPrimary,
    surface = Card,
    onSurface = TextPrimary,
)

@Composable
fun NikaaloTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NikaaloColors, content = content)
}

fun vehicleColor(type: String): Color = when (type) {
    "auto" -> Accent
    "bike" -> Color(0xFF9B7BFF)
    "car" -> Color(0xFF4FA8FF)
    "thela" -> Color(0xFFFF8A4C)
    "bus" -> Color(0xFF2ED3B7)
    "truck" -> Color(0xFFFF6B9D)
    "cow" -> Color(0xFFA1785C)
    else -> Color(0xFF8C9AAB)
}
