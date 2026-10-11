package com.nothing.tune.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.tune.player.AudioPlayerController
import com.nothing.tune.ui.theme.PureBlack

@Composable
fun EqualizerScreen(
    controller: AudioPlayerController,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by controller.uiState.collectAsState()
    val presets = listOf("Custom", "Bass Boost", "Vocal", "Balanced", "Treble Boost", "Lo-Fi")

    val numBands = uiState.eqFreqs.size.coerceAtMost(7)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Top Bar ────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Equalizer",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Switch(
                checked = uiState.eqEnabled,
                onCheckedChange = { controller.toggleEq(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF444448),
                    uncheckedThumbColor = Color(0xFF888888),
                    uncheckedTrackColor = Color(0xFF1F1F22)
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ── Preset Pills Row ───────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            presets.forEach { preset ->
                val isSelected = uiState.currentPreset == preset
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color.White else Color(0xFF16161A))
                        .border(
                            1.dp,
                            if (isSelected) Color.White else Color(0xFF262628),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { controller.applyPreset(preset) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = preset,
                        color = if (isSelected) Color.Black else Color(0xFFCCCCCC),
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Vertical Faders Section ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF141416))
                .border(1.dp, Color(0xFF262628), RoundedCornerShape(20.dp))
                .padding(vertical = 20.dp, horizontal = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // dB Scale on Left (+12, 0, -12)
                Column(
                    modifier = Modifier
                        .height(210.dp)
                        .padding(end = 6.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text("+12", color = Color(0xFF666666), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("0", color = Color(0xFF666666), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("-12", color = Color(0xFF666666), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Interactive Sliders
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(230.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until numBands) {
                        val level = if (i in uiState.eqBands.indices) uiState.eqBands[i].toInt() else 0
                        val rawFreq = uiState.eqFreqs.getOrElse(i) { (60 * (i + 1)) }
                        val freqText = if (rawFreq >= 1000) "${rawFreq / 1000}K" else "${rawFreq}"

                        VerticalFader(
                            level = level,
                            label = freqText,
                            enabled = uiState.eqEnabled,
                            onLevelChange = { newLevel ->
                                controller.setEqBandLevel(i, newLevel.toShort())
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Hardware DSP Cards ─────────────────────────────────────────────────
        // 1. Bass Enhancer Card
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
            Column {
                Text(
                    text = "Bass Enhancer",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (uiState.bassBoost > 0) "${uiState.bassBoost / 10}% Boosted" else "Off",
                    color = Color(0xFF888888),
                    fontSize = 11.sp
                )
            }

            Switch(
                checked = uiState.bassBoost > 0,
                onCheckedChange = {
                    controller.setBassBoost(if (it) 650 else 0)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF444448),
                    uncheckedThumbColor = Color(0xFF888888),
                    uncheckedTrackColor = Color(0xFF1F1F22)
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. 3D Audio Card
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
            Column {
                Text(
                    text = "3D Audio Virtualizer",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (uiState.virtualizer > 0) "${uiState.virtualizer / 10}% Surround" else "Off",
                    color = Color(0xFF888888),
                    fontSize = 11.sp
                )
            }

            Switch(
                checked = uiState.virtualizer > 0,
                onCheckedChange = {
                    controller.setVirtualizer(if (it) 700 else 0)
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
    }
}

@Composable
private fun VerticalFader(
    level: Int,
    label: String,
    enabled: Boolean,
    onLevelChange: (Int) -> Unit
) {
    val initialFrac = ((level + 1500f) / 3000f).coerceIn(0f, 1f)
    var dragFrac by remember(level) { mutableFloatStateOf(initialFrac) }

    val dbValue = (level / 100) // approx dB readout
    val dbText = if (dbValue > 0) "+$dbValue" else "$dbValue"

    Column(
        modifier = Modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // dB Readout at Top of Fader
        Text(
            text = dbText,
            color = if (enabled && level != 0) Color.White else Color(0xFF666666),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )

        // Vertical track rail (wide touch target with drag and tap)
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(175.dp)
                .pointerInput(enabled) {
                    if (enabled) {
                        detectVerticalDragGestures(
                            onDragStart = {
                                dragFrac = ((level + 1500f) / 3000f).coerceIn(0f, 1f)
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            val h = size.height.toFloat()
                            val deltaFrac = -dragAmount / h
                            dragFrac = (dragFrac + deltaFrac).coerceIn(0f, 1f)
                            val newLevel = ((dragFrac * 3000f) - 1500f).toInt()
                            onLevelChange(newLevel)
                        }
                    }
                }
                .pointerInput(enabled) {
                    if (enabled) {
                        detectTapGestures { offset ->
                            val h = size.height.toFloat()
                            dragFrac = (1f - offset.y / h).coerceIn(0f, 1f)
                            val newLevel = ((dragFrac * 3000f) - 1500f).toInt()
                            onLevelChange(newLevel)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val h = size.height

                // Track Line
                drawLine(
                    color = Color(0xFF2A2A2E),
                    start = Offset(cx, 0f),
                    end = Offset(cx, h),
                    strokeWidth = 2.5.dp.toPx()
                )

                // Knob position
                val knobY = (1f - dragFrac) * h
                drawCircle(
                    color = if (enabled) Color.White else Color(0xFF666666),
                    radius = 9.dp.toPx(),
                    center = Offset(cx, knobY)
                )

                // Center inner dot on knob for Nothing OS hardware dial aesthetic
                drawCircle(
                    color = if (enabled) Color.Black else Color(0xFF333333),
                    radius = 3.dp.toPx(),
                    center = Offset(cx, knobY)
                )
            }
        }

        // Frequency Label at Bottom of Fader
        Text(
            text = label,
            color = Color(0xFF888888),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
