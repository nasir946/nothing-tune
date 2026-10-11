package com.nothing.tune.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.tune.player.AudioPlayerController
import com.nothing.tune.ui.theme.PureBlack
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SleepTimerScreen(
    controller: AudioPlayerController,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by controller.uiState.collectAsState()
    var selectedMinutes by remember { mutableIntStateOf(30) }
    var stopAfterSong by remember { mutableStateOf(uiState.stopAfterCurrentSong) }

    val presetOptions = listOf(0, 15, 30, 60, 90)

    val remainingSec = uiState.sleepTimerRemainingSeconds
    val isTimerActive = remainingSec != null && remainingSec > 0
    val displayMinutes = if (isTimerActive) ((remainingSec ?: 0) + 59) / 60 else selectedMinutes

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(horizontal = 24.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ── Top Bar ────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Sleep Timer",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Circular Dial Display ──────────────────────────────────────────────
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val radius = size.width * 0.44f

                // Inactive track arc
                drawCircle(
                    color = Color(0xFF1E1E22),
                    radius = radius,
                    center = Offset(cx, cy),
                    style = Stroke(width = 3.dp.toPx())
                )

                // Active progress arc
                val sweep = ((displayMinutes.toFloat() / 90f).coerceIn(0f, 1f)) * 360f
                drawArc(
                    color = Color.White,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(cx - radius, cy - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )

                // Indicator thumb dot
                val angleRad = Math.toRadians((sweep - 90.0))
                val dotX = cx + (radius * cos(angleRad)).toFloat()
                val dotY = cy + (radius * sin(angleRad)).toFloat()
                drawCircle(
                    color = Color.White,
                    radius = 6.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Nightlight,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (displayMinutes == 0) "Off" else "$displayMinutes",
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (displayMinutes == 0) "" else "minutes",
                    color = Color(0xFF888888),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Preset Pills Row (Off, 15, 30, 60, 90) ─────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            presetOptions.forEach { mins ->
                val isSelected = selectedMinutes == mins
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color.White else Color(0xFF16161A))
                        .border(
                            1.dp,
                            if (isSelected) Color.White else Color(0xFF262628),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            selectedMinutes = mins
                            if (isTimerActive) {
                                controller.startSleepTimer(mins, stopAfterSong)
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (mins == 0) "Off" else "$mins",
                        color = if (isSelected) Color.Black else Color(0xFFCCCCCC),
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── "Stop after current song" Toggle Card ──────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF16161A))
                .border(1.dp, Color(0xFF262628), RoundedCornerShape(16.dp))
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Stop after current song",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Switch(
                checked = stopAfterSong,
                onCheckedChange = {
                    stopAfterSong = it
                    controller.setStopAfterCurrentSong(it)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF444448),
                    uncheckedThumbColor = Color(0xFF888888),
                    uncheckedTrackColor = Color(0xFF1F1F22)
                )
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Big CTA Button ("Start Timer" / "Stop Timer") ──────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(if (isTimerActive) Color(0xFF2A2A2E) else Color.White)
                .clickable {
                    if (isTimerActive) {
                        controller.cancelSleepTimer()
                    } else {
                        if (selectedMinutes > 0) {
                            controller.startSleepTimer(selectedMinutes, stopAfterSong)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isTimerActive) "Stop Timer" else "Start Timer",
                color = if (isTimerActive) Color.White else Color.Black,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
    }
}
