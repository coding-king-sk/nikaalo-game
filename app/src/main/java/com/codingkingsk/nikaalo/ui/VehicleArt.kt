package com.codingkingsk.nikaalo.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate

/**
 * Top-down vehicle art drawn with Canvas - no image assets, scales to any cell size.
 * Vertical vehicles are drawn in a rotated coordinate space so one routine covers both.
 */
@Composable
fun VehicleArt(type: String, horizontal: Boolean, modifier: Modifier = Modifier) {
    val base = vehicleColor(type)
    Canvas(modifier = modifier) {
        if (type == "cow") drawCow(base) else drawVehicle(base, horizontal, type == "auto")
    }
}

private fun DrawScope.drawVehicle(base: Color, horizontal: Boolean, isPlayer: Boolean) {
    rotate(if (horizontal) 0f else 90f) {
        val length = if (horizontal) size.width else size.height
        val width = if (horizontal) size.height else size.width
        val left = (size.width - length) / 2f
        val top = (size.height - width) / 2f

        val tyre = Color(0xFF11151A)
        val tyreThickness = width * 0.13f
        val tyreLength = length * 0.17f
        for (fraction in listOf(0.12f, 0.66f)) {
            val x = left + length * fraction
            drawRoundRect(
                color = tyre,
                topLeft = Offset(x, top + width * 0.02f),
                size = Size(tyreLength, tyreThickness),
                cornerRadius = CornerRadius(tyreThickness / 2f),
            )
            drawRoundRect(
                color = tyre,
                topLeft = Offset(x, top + width - tyreThickness - width * 0.02f),
                size = Size(tyreLength, tyreThickness),
                cornerRadius = CornerRadius(tyreThickness / 2f),
            )
        }

        val bodyTop = top + width * 0.10f
        val bodyHeight = width * 0.80f
        val bodyLeft = left + length * 0.015f
        val bodyLength = length * 0.97f
        val corner = CornerRadius(width * 0.26f)

        drawRoundRect(base, Offset(bodyLeft, bodyTop), Size(bodyLength, bodyHeight), corner)
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.16f),
            topLeft = Offset(bodyLeft, bodyTop + bodyHeight * 0.62f),
            size = Size(bodyLength, bodyHeight * 0.38f),
            cornerRadius = corner,
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.14f),
            topLeft = Offset(bodyLeft, bodyTop),
            size = Size(bodyLength, bodyHeight * 0.26f),
            cornerRadius = corner,
        )

        val glass = Color(0xFF0D1319).copy(alpha = 0.75f)
        drawRoundRect(
            color = glass,
            topLeft = Offset(bodyLeft + bodyLength * 0.24f, bodyTop + bodyHeight * 0.17f),
            size = Size(bodyLength * 0.30f, bodyHeight * 0.66f),
            cornerRadius = CornerRadius(width * 0.13f),
        )
        drawRoundRect(
            color = glass,
            topLeft = Offset(bodyLeft + bodyLength * 0.64f, bodyTop + bodyHeight * 0.19f),
            size = Size(bodyLength * 0.13f, bodyHeight * 0.62f),
            cornerRadius = CornerRadius(width * 0.10f),
        )

        val lamp = Color(0xFFFFF6D0)
        drawRoundRect(
            color = lamp,
            topLeft = Offset(bodyLeft + bodyLength * 0.92f, bodyTop + bodyHeight * 0.10f),
            size = Size(bodyLength * 0.05f, bodyHeight * 0.22f),
            cornerRadius = CornerRadius(width * 0.04f),
        )
        drawRoundRect(
            color = lamp,
            topLeft = Offset(bodyLeft + bodyLength * 0.92f, bodyTop + bodyHeight * 0.68f),
            size = Size(bodyLength * 0.05f, bodyHeight * 0.22f),
            cornerRadius = CornerRadius(width * 0.04f),
        )

        drawRoundRect(
            color = Color.Black.copy(alpha = if (isPlayer) 0.55f else 0.30f),
            topLeft = Offset(bodyLeft, bodyTop),
            size = Size(bodyLength, bodyHeight),
            cornerRadius = corner,
            style = Stroke(width = width * 0.05f),
        )
    }
}

private fun DrawScope.drawCow(base: Color) {
    val inset = size.minDimension * 0.10f
    val body = Size(size.width - inset * 2f, size.height - inset * 2f)
    val corner = CornerRadius(size.minDimension * 0.32f)
    drawRoundRect(base, Offset(inset, inset), body, corner)

    val spot = Color(0xFF3B2A20).copy(alpha = 0.55f)
    drawCircle(spot, radius = size.minDimension * 0.14f, center = Offset(size.width * 0.36f, size.height * 0.40f))
    drawCircle(spot, radius = size.minDimension * 0.10f, center = Offset(size.width * 0.63f, size.height * 0.63f))
    drawCircle(Color(0xFFF6E7D6), radius = size.minDimension * 0.08f, center = Offset(size.width * 0.66f, size.height * 0.34f))

    drawRoundRect(
        color = Color.Black.copy(alpha = 0.32f),
        topLeft = Offset(inset, inset),
        size = body,
        cornerRadius = corner,
        style = Stroke(width = size.minDimension * 0.06f),
    )
}
