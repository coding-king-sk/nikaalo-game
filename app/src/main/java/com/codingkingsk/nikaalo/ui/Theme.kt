package com.codingkingsk.nikaalo.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Background = Color(0xFF0E1116)
val SurfaceDark = Color(0xFF171C24)
val BoardBg = Color(0xFF222A35)
val CellBg = Color(0xFF2B333F)
val Accent = Color(0xFFFFC107)
val Success = Color(0xFF4CAF50)
val TextPrimary = Color(0xFFF2F5F9)
val TextMuted = Color(0xFF9AA6B5)

private val NikaaloColors = darkColorScheme(
	primary = Accent,
	onPrimary = Color(0xFF1A1300),
	secondary = Success,
	background = Background,
	onBackground = TextPrimary,
	surface = SurfaceDark,
	onSurface = TextPrimary,
)

@Composable
fun NikaaloTheme(content: @Composable () -> Unit) {
	MaterialTheme(colorScheme = NikaaloColors, content = content)
}

fun vehicleColor(type: String): Color = when (type) {
	"auto" -> Color(0xFFFFC107)
	"bike" -> Color(0xFF7E57C2)
	"car" -> Color(0xFF42A5F5)
	"thela" -> Color(0xFFFF7043)
	"bus" -> Color(0xFF26A69A)
	"truck" -> Color(0xFFEC407A)
	"cow" -> Color(0xFF8D6E63)
	else -> Color(0xFF90A4AE)
}

fun vehicleEmoji(type: String): String = when (type) {
	"auto" -> "\uD83D\uDEFA"
	"bike" -> "\uD83C\uDFCD"
	"car" -> "\uD83D\uDE97"
	"thela" -> "\uD83D\uDED2"
	"bus" -> "\uD83D\uDE8C"
	"truck" -> "\uD83D\uDE9A"
	"cow" -> "\uD83D\uDC04"
	else -> "\u25AA"
}
