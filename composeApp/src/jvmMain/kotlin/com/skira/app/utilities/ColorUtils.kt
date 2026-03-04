package com.skira.app.utilities

import androidx.compose.ui.graphics.Color

fun parseHexToColor(hex: String): Color {
    val s = hex.trim().removePrefix("#")
    val v = s.toLongOrNull(16) ?: return Color.Unspecified
    return when (s.length) {
        6 -> {
            val r = ((v shr 16) and 0xFF).toInt()
            val g = ((v shr 8) and 0xFF).toInt()
            val b = (v and 0xFF).toInt()
            Color(r / 255f, g / 255f, b / 255f, 1f)
        }
        8 -> {
            val a = ((v shr 24) and 0xFF).toInt()
            val r = ((v shr 16) and 0xFF).toInt()
            val g = ((v shr 8) and 0xFF).toInt()
            val b = (v and 0xFF).toInt()
            Color(r / 255f, g / 255f, b / 255f, a / 255f)
        }
        else -> Color.Unspecified
    }
}

fun safeGradientColors(colors: List<Color>): List<Color> {
    return when {
        colors.isEmpty() -> listOf(Color.LightGray, Color.LightGray)
        colors.size == 1 -> listOf(colors[0], colors[0])
        else -> colors
    }
}

