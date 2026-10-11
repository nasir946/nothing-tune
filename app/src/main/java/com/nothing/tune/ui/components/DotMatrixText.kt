package com.nothing.tune.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nothing.tune.ui.theme.AccentRed

// 5x7 dot-matrix bitmap representations
private val GLYPHS: Map<Char, List<Int>> = mapOf(
    'A' to listOf(0b01110, 0b10001, 0b10001, 0b11111, 0b10001, 0b10001, 0b10001),
    'B' to listOf(0b11110, 0b10001, 0b10001, 0b11110, 0b10001, 0b10001, 0b11110),
    'C' to listOf(0b01111, 0b10000, 0b10000, 0b10000, 0b10000, 0b10000, 0b01111),
    'D' to listOf(0b11110, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b11110),
    'E' to listOf(0b11111, 0b10000, 0b10000, 0b11110, 0b10000, 0b10000, 0b11111),
    'F' to listOf(0b11111, 0b10000, 0b10000, 0b11110, 0b10000, 0b10000, 0b10000),
    'G' to listOf(0b01111, 0b10000, 0b10000, 0b10111, 0b10001, 0b10001, 0b01111),
    'H' to listOf(0b10001, 0b10001, 0b10001, 0b11111, 0b10001, 0b10001, 0b10001),
    'I' to listOf(0b11111, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b11111),
    'J' to listOf(0b00001, 0b00001, 0b00001, 0b00001, 0b10001, 0b10001, 0b01110),
    'K' to listOf(0b10001, 0b10010, 0b10100, 0b11000, 0b10100, 0b10010, 0b10001),
    'L' to listOf(0b10000, 0b10000, 0b10000, 0b10000, 0b10000, 0b10000, 0b11111),
    'M' to listOf(0b10001, 0b11011, 0b10101, 0b10001, 0b10001, 0b10001, 0b10001),
    'N' to listOf(0b10001, 0b11001, 0b10101, 0b10011, 0b10001, 0b10001, 0b10001),
    'O' to listOf(0b01110, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01110),
    'P' to listOf(0b11110, 0b10001, 0b10001, 0b11110, 0b10000, 0b10000, 0b10000),
    'Q' to listOf(0b01110, 0b10001, 0b10001, 0b10001, 0b10101, 0b10011, 0b01111),
    'R' to listOf(0b11110, 0b10001, 0b10001, 0b11110, 0b10100, 0b10010, 0b10001),
    'S' to listOf(0b01111, 0b10000, 0b10000, 0b01110, 0b00001, 0b00001, 0b11110),
    'T' to listOf(0b11111, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100),
    'U' to listOf(0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01110),
    'V' to listOf(0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01010, 0b00100),
    'W' to listOf(0b10001, 0b10001, 0b10001, 0b10101, 0b10101, 0b11011, 0b10001),
    'X' to listOf(0b10001, 0b01010, 0b00100, 0b00100, 0b00100, 0b01010, 0b10001),
    'Y' to listOf(0b10001, 0b10001, 0b01010, 0b00100, 0b00100, 0b00100, 0b00100),
    'Z' to listOf(0b11111, 0b00001, 0b00010, 0b00100, 0b01000, 0b10000, 0b11111),
    '0' to listOf(0b01110, 0b10011, 0b10101, 0b11001, 0b10001, 0b10001, 0b01110),
    '1' to listOf(0b00100, 0b01100, 0b00100, 0b00100, 0b00100, 0b00100, 0b01110),
    '2' to listOf(0b01110, 0b10001, 0b00001, 0b00010, 0b00100, 0b01000, 0b11111),
    '3' to listOf(0b11110, 0b00001, 0b00010, 0b01100, 0b00010, 0b00001, 0b11110),
    '4' to listOf(0b00010, 0b00110, 0b01010, 0b10010, 0b11111, 0b00010, 0b00010),
    '5' to listOf(0b11111, 0b10000, 0b11110, 0b00001, 0b00001, 0b10001, 0b01110),
    '6' to listOf(0b00110, 0b01000, 0b10000, 0b11110, 0b10001, 0b10001, 0b01110),
    '7' to listOf(0b11111, 0b00001, 0b00010, 0b00100, 0b01000, 0b01000, 0b01000),
    '8' to listOf(0b01110, 0b10001, 0b10001, 0b01110, 0b10001, 0b10001, 0b01110),
    '9' to listOf(0b01110, 0b10001, 0b10001, 0b01111, 0b00001, 0b00010, 0b01100),
    ':' to listOf(0b00000, 0b00100, 0b00000, 0b00000, 0b00100, 0b00000, 0b00000),
    '.' to listOf(0b00000, 0b00000, 0b00000, 0b00000, 0b00000, 0b00100, 0b00100),
    ',' to listOf(0b00000, 0b00000, 0b00000, 0b00000, 0b00100, 0b00100, 0b01000),
    '\'' to listOf(0b00100, 0b00100, 0b01000, 0b00000, 0b00000, 0b00000, 0b00000),
    '-' to listOf(0b00000, 0b00000, 0b00000, 0b11111, 0b00000, 0b00000, 0b00000),
    '!' to listOf(0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b00000, 0b00100),
    '?' to listOf(0b01110, 0b10001, 0b00010, 0b00100, 0b00100, 0b00000, 0b00100),
    '•' to listOf(0b00000, 0b00000, 0b01110, 0b01110, 0b01110, 0b00000, 0b00000),
    ' ' to listOf(0b00000, 0b00000, 0b00000, 0b00000, 0b00000, 0b00000, 0b00000)
)

@Composable
fun DotMatrixText(
    text: String,
    modifier: Modifier = Modifier,
    dotSize: Dp = 2.2.dp,
    dotSpacing: Dp = 1.2.dp,
    activeColor: Color = Color.White,
    showDimGrid: Boolean = true,
    dimColor: Color = Color(0xFF262628).copy(alpha = 0.5f),
    hasSignatureRedDot: Boolean = false
) {
    val density = LocalDensity.current
    val dotPx = with(density) { dotSize.toPx() }
    val spacePx = with(density) { dotSpacing.toPx() }
    val step = dotPx + spacePx

    val charCols = 5
    val charRows = 7
    val charSpacingPx = step * 1.5f

    val chars = text.uppercase()
    val totalWidthPx = chars.indices.sumOf {
        (charCols * step + charSpacingPx).toDouble()
    }.toFloat() + if (hasSignatureRedDot) (step * 5) else 0f
    val totalHeightPx = charRows * step

    val widthDp = with(density) { totalWidthPx.toDp() }
    val heightDp = with(density) { totalHeightPx.toDp() }

    Canvas(
        modifier = modifier
            .width(widthDp)
            .height(heightDp)
    ) {
        var xOffset = 0f

        chars.forEach { ch ->
            val glyph = GLYPHS[ch] ?: GLYPHS[' ']?.takeIf { ch == ' ' } ?: GLYPHS['?'] ?: List(7) { 0 }

            for (r in 0 until charRows) {
                val rowBits = if (r in glyph.indices) glyph[r] else 0
                for (c in 0 until charCols) {
                    val bit = (rowBits shr (charCols - 1 - c)) and 1
                    val cx = xOffset + c * step + dotPx / 2
                    val cy = r * step + dotPx / 2

                    if (bit == 1) {
                        drawCircle(
                            color = activeColor,
                            radius = dotPx / 2,
                            center = Offset(cx, cy)
                        )
                    } else if (showDimGrid && ch != ' ') {
                        drawCircle(
                            color = dimColor,
                            radius = (dotPx / 2) * 0.7f,
                            center = Offset(cx, cy)
                        )
                    }
                }
            }
            xOffset += charCols * step + charSpacingPx
        }

        if (hasSignatureRedDot) {
            val redDotRadius = dotPx * 1.3f
            val redDotCenter = Offset(xOffset + redDotRadius, totalHeightPx / 2f)
            drawCircle(
                color = AccentRed,
                radius = redDotRadius,
                center = redDotCenter
            )
        }
    }
}

@Composable
fun DotMatrixLogo(
    modifier: Modifier = Modifier,
    dotSize: Dp = 3.dp,
    dotSpacing: Dp = 1.4.dp
) {
    DotMatrixText(
        text = "MUSIC",
        modifier = modifier,
        dotSize = dotSize,
        dotSpacing = dotSpacing,
        activeColor = Color.White,
        showDimGrid = true,
        hasSignatureRedDot = true
    )
}
