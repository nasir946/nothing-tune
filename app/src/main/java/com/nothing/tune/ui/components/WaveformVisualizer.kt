package com.nothing.tune.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

enum class VisualizerStyle {
    DOT_MATRIX,
    BARS,
    ONE_UI_WAVE
}

@Composable
fun WaveformVisualizer(
    isPlaying: Boolean,
    style: VisualizerStyle = VisualizerStyle.BARS,
    accentColor: Color = Color.White,
    magnitudes: FloatArray? = null,
    onStyleChange: (() -> Unit)? = null,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier.clickable(enabled = onStyleChange != null) {
            onStyleChange?.invoke()
        }
    ) {
        val width = size.width
        val height = size.height

        when (style) {
            VisualizerStyle.DOT_MATRIX -> {
                val barCount = 24
                val dotsPerBar = 7
                val stepX = width / barCount
                val stepY = height / dotsPerBar
                val dotRadius = (stepX * 0.28f).coerceAtMost(stepY * 0.28f)

                for (col in 0 until barCount) {
                    val x = col * stepX + stepX / 2

                    val frac = if (isPlaying) {
                        if (magnitudes != null && col in magnitudes.indices) {
                            magnitudes[col].coerceIn(0.12f, 1.0f)
                        } else {
                            val p = phase + (col.toFloat() / barCount) * Math.PI.toFloat() * 2
                            ((sin(p) + cos(p * 1.5f) + 2f) / 4f).coerceIn(0.12f, 1f)
                        }
                    } else {
                        0.14f
                    }

                    val activeDots = (frac * dotsPerBar).toInt().coerceIn(1, dotsPerBar)

                    for (row in 0 until dotsPerBar) {
                        val y = height - (row * stepY + stepY / 2)
                        val isLit = row < activeDots
                        drawCircle(
                            color = if (isLit) accentColor else accentColor.copy(alpha = 0.12f),
                            radius = dotRadius,
                            center = Offset(x, y)
                        )
                    }
                }
            }

            VisualizerStyle.BARS -> {
                val barCount = 26
                val barWidth = (width / barCount) * 0.58f
                val gap = (width / barCount) * 0.42f

                for (i in 0 until barCount) {
                    val x = i * (barWidth + gap)
                    val fraction = if (isPlaying) {
                        if (magnitudes != null && i in magnitudes.indices) {
                            magnitudes[i].coerceIn(0.12f, 1.0f)
                        } else {
                            val p = phase + (i.toFloat() / barCount) * Math.PI.toFloat() * 2
                            val wave = (sin(p) + cos(p * 1.7f) + 2f) / 4f
                            wave.coerceIn(0.15f, 1f)
                        }
                    } else {
                        0.12f
                    }

                    val barHeight = (height * fraction).coerceAtLeast(4.dp.toPx())
                    val top = height - barHeight

                    // Subtle top accent dot/peak for Nothing OS physical look
                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset(x, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }

            VisualizerStyle.ONE_UI_WAVE -> {
                val midY = height / 2
                val strokePath = Path().apply { moveTo(0f, midY) }
                val fillPath = Path().apply {
                    moveTo(0f, height)
                    lineTo(0f, midY)
                }

                val segments = 40
                val dx = width / segments

                for (i in 1..segments) {
                    val x = i * dx
                    val magIdx = ((i.toFloat() / segments) * (magnitudes?.size ?: 26)).toInt().coerceIn(0, (magnitudes?.size ?: 26) - 1)
                    val mag = magnitudes?.getOrNull(magIdx) ?: 0.5f

                    val y = if (isPlaying) {
                        val p = phase + (i.toFloat() / segments) * Math.PI.toFloat() * 3
                        midY - (mag - 0.5f) * height * 0.8f + (sin(p) * 0.15f) * height
                    } else {
                        midY
                    }
                    strokePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }

                fillPath.lineTo(width, height)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(accentColor.copy(alpha = 0.35f), accentColor.copy(alpha = 0.02f)),
                        startY = 0f,
                        endY = height
                    )
                )

                drawPath(
                    path = strokePath,
                    color = accentColor,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }
    }
}
