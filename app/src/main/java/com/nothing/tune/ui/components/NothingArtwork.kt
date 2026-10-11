package com.nothing.tune.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nothing.tune.data.MusicTrack
import com.nothing.tune.ui.theme.AccentRed
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NothingArtwork(
    track: MusicTrack?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    seed: Int = 0,
    isCircle: Boolean = false
) {
    val shape = if (isCircle) CircleShape else RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFF141416))
            .border(1.dp, Color(0xFF262628), shape),
        contentAlignment = Alignment.Center
    ) {
        val artUri = track?.artworkUri

        if (artUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(artUri)
                    .crossfade(true)
                    .build(),
                contentDescription = track.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Procedural Nothing OS cover art generator
            ProceduralNothingArt(
                title = track?.title ?: "Nothing",
                artist = track?.artist ?: "Unknown",
                seed = if (track != null) abs(track.id.toInt()) else seed
            )
        }
    }
}

@Composable
fun ProceduralNothingArt(
    title: String,
    artist: String,
    seed: Int,
    modifier: Modifier = Modifier
) {
    val styleIndex = abs(seed) % 4

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        when (styleIndex) {
            // Style 0: Moon phase / Celestial sphere (like the Onboarding & "Midnight" art)
            0 -> {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF202024), Color(0xFF09090A)),
                        center = Offset(cx, cy),
                        radius = w * 0.8f
                    )
                )
                // Left half dark, right half illuminated crater texture
                val r = (w * 0.38f)
                drawCircle(color = Color(0xFF161618), radius = r, center = Offset(cx, cy))
                drawArc(
                    color = Color(0xFFDDDDDF),
                    startAngle = 270f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(cx - r, cy - r),
                    size = Size(r * 2, r * 2)
                )
                // Red dot accent
                drawCircle(color = AccentRed, radius = 4.dp.toPx(), center = Offset(cx + r * 0.5f, cy - r * 0.3f))
                // Grid dots
                for (i in -4..4) {
                    for (j in -4..4) {
                        val px = cx + i * 16.dp.toPx()
                        val py = cy + j * 16.dp.toPx()
                        if ((i + j) % 2 == 0) {
                            drawCircle(color = Color.White.copy(alpha = 0.08f), radius = 1.dp.toPx(), center = Offset(px, py))
                        }
                    }
                }
            }

            // Style 1: Concentric sound orbits (like "Chasing Light")
            1 -> {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1B1B1E), Color(0xFF0A0A0C))
                    )
                )
                for (step in 1..6) {
                    val radius = (w * 0.08f) * step
                    drawCircle(
                        color = Color.White.copy(alpha = 0.06f + step * 0.02f),
                        radius = radius,
                        center = Offset(cx, cy),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
                // Center core
                drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(cx, cy))
            }

            // Style 2: Crimson & Dark Silhouette (like "Retrograde")
            2 -> {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(AccentRed.copy(alpha = 0.85f), Color(0xFF120304), Color(0xFF080808)),
                        center = Offset(cx * 1.2f, cy * 0.8f),
                        radius = w * 0.9f
                    )
                )
                // Dark silhouette arch
                drawCircle(
                    color = Color(0xFF0A0A0C),
                    radius = w * 0.35f,
                    center = Offset(cx, cy + h * 0.2f)
                )
                drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(cx * 0.8f, cy * 0.7f))
            }

            // Style 3: Minimal matrix soundwave field
            else -> {
                drawRect(color = Color(0xFF111114))
                val cols = 12
                val colW = w / (cols + 1)
                for (c in 1..cols) {
                    val colX = c * colW
                    val barH = (sin((c + seed) * 0.8) * 0.35 + 0.45).toFloat() * (h * 0.6f)
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(colX, cy - barH / 2f),
                        end = Offset(colX, cy + barH / 2f),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }
        }
    }
}
