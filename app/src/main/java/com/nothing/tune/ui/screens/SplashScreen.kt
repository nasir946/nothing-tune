package com.nothing.tune.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.tune.ui.components.DotMatrixLogo
import com.nothing.tune.ui.theme.PureBlack
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun SplashScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alphaAnim = remember { Animatable(0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "soundbars")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))
        delay(1600)
        onFinish()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack),
        contentAlignment = Alignment.Center
    ) {
        // Vertical background sound bars (Screen 1 style)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            val barCount = 7
            val barWidth = 32.dp.toPx()
            val spacing = 20.dp.toPx()
            val totalSpan = barCount * barWidth + (barCount - 1) * spacing
            val startX = cx - totalSpan / 2f

            for (i in 0 until barCount) {
                val bx = startX + i * (barWidth + spacing)
                val baseH = when (i) {
                    0, 6 -> h * 0.28f
                    1, 5 -> h * 0.42f
                    2, 4 -> h * 0.58f
                    else -> h * 0.72f
                }
                val dynamicFactor = 0.85f + 0.15f * sin(phase + i * 0.8f)
                val barH = baseH * dynamicFactor
                val by = cy - barH / 2f

                drawRoundRect(
                    color = Color(0xFF161618),
                    topLeft = Offset(bx, by),
                    size = Size(barWidth, barH),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
            }
        }

        // Center Dot Matrix Logo
        DotMatrixLogo(
            dotSize = 5.dp,
            dotSpacing = 2.4.dp
        )

        // Bottom tagline "Feel the Sound"
        Text(
            text = "Feel  the  Sound",
            color = Color(0xFF888888),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 4.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 54.dp)
        )
    }
}
