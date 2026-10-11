package com.nothing.tune.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

enum class KnobMode {
    VOLUME,
    EQUALIZER
}

@Composable
fun RoundVolumeKnob(
    volume: Float,
    eqBandLevel: Short,
    mode: KnobMode,
    accentColor: Color,
    onVolumeChange: (Float) -> Unit,
    onEqGainChange: (Short) -> Unit,
    onModeToggle: () -> Unit,
    onExpandEqToggle: () -> Unit,
    isEqExpanded: Boolean,
    eqBands: List<Short>,
    eqFreqs: List<Int>,
    selectedBand: Int,
    onSelectBand: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Expandable EQ Bars Strip (if toggled)
        if (isEqExpanded) {
            InlineEqBarsStrip(
                eqBands = eqBands,
                eqFreqs = eqFreqs,
                selectedBand = selectedBand,
                accentColor = accentColor,
                onBandChange = { band, level ->
                    onEqGainChange(level)
                    onSelectBand(band)
                },
                onSelectBand = onSelectBand
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Control chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = mode == KnobMode.VOLUME,
                onClick = onModeToggle,
                label = { Text("VOL MODE", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = accentColor,
                    selectedLabelColor = Color.Black
                )
            )

            FilterChip(
                selected = mode == KnobMode.EQUALIZER,
                onClick = onModeToggle,
                label = { Text("EQ MODE", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFFB300),
                    selectedLabelColor = Color.Black
                )
            )

            FilterChip(
                selected = isEqExpanded,
                onClick = onExpandEqToggle,
                label = { Text(if (isEqExpanded) "HIDE BARS" else "EQ BARS", fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Rotatable Round Knob
        val isEq = mode == KnobMode.EQUALIZER
        val fraction = if (!isEq) {
            volume.coerceIn(0f, 1f)
        } else {
            ((eqBandLevel + 1500f) / 3000f).coerceIn(0f, 1f)
        }

        var dragFraction by remember(fraction) { mutableFloatStateOf(fraction) }

        Box(
            modifier = Modifier
                .size(86.dp)
                .clip(CircleShape)
                .pointerInput(mode) {
                    var lastOffset: Offset? = null
                    detectDragGestures(
                        onDragStart = { offset -> lastOffset = offset },
                        onDrag = { change, _ ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val prev = (lastOffset ?: change.position) - center
                            val curr = change.position - center

                            val prevAngle = atan2(prev.y, prev.x)
                            val currAngle = atan2(curr.y, curr.x)
                            var delta = currAngle - prevAngle

                            if (delta > Math.PI) delta -= (2 * Math.PI).toFloat()
                            if (delta < -Math.PI) delta += (2 * Math.PI).toFloat()

                            val totalSweep = (270f * Math.PI / 180f).toFloat()
                            val newFrac = (dragFraction + delta / totalSweep).coerceIn(0f, 1f)
                            dragFraction = newFrac

                            if (!isEq) {
                                onVolumeChange(newFrac)
                            } else {
                                val milliDb = ((newFrac * 3000) - 1500).toInt().toShort()
                                onEqGainChange(milliDb)
                            }
                            lastOffset = change.position
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(86.dp)) {
                val strokeWidth = 5.dp.toPx()
                val radius = size.width / 2 - strokeWidth
                val center = Offset(size.width / 2, size.height / 2)

                // Track
                drawArc(
                    color = Color(0xFF262626),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = Size(radius * 2, radius * 2),
                    topLeft = Offset(center.x - radius, center.y - radius)
                )

                // Progress Arc
                val activeColor = if (isEq) Color(0xFFFFB300) else accentColor
                drawArc(
                    color = activeColor,
                    startAngle = 135f,
                    sweepAngle = 270f * dragFraction,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = Size(radius * 2, radius * 2),
                    topLeft = Offset(center.x - radius, center.y - radius)
                )

                // Knob Center circle
                drawCircle(
                    color = Color(0xFF141414),
                    radius = radius - strokeWidth - 2.dp.toPx()
                )
            }

            // Text Readout
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (!isEq) {
                    Text(
                        text = "${(dragFraction * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("VOL", color = Color(0xFF888888), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                } else {
                    val db = ((dragFraction * 30) - 15).toInt()
                    Text(
                        text = if (db > 0) "+$db" else "$db",
                        color = Color(0xFFFFB300),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    val freq = if (selectedBand in eqFreqs.indices) eqFreqs[selectedBand] else 1000
                    val freqLabel = if (freq >= 1000) "${freq / 1000}k" else "${freq}Hz"
                    Text(freqLabel, color = Color(0xFFFFB300), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun InlineEqBarsStrip(
    eqBands: List<Short>,
    eqFreqs: List<Int>,
    selectedBand: Int,
    accentColor: Color,
    onBandChange: (Int, Short) -> Unit,
    onSelectBand: (Int) -> Unit
) {
    val displayBands = listOf(0, 2, 4, 6, 9)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF141416).copy(alpha = 0.9f))
            .border(1.dp, Color(0xFF262626), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column {
            Text(
                text = "EQUALIZER BARS (DRAG TO ADJUST)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                displayBands.forEach { bandIdx ->
                    if (bandIdx in eqBands.indices) {
                        val level = eqBands[bandIdx]
                        val isSelected = selectedBand == bandIdx
                        val freq = if (bandIdx in eqFreqs.indices) eqFreqs[bandIdx] else 1000
                        val freqLabel = if (freq >= 1000) "${freq / 1000}k" else "$freq"
                        val fraction = ((level + 1500f) / 3000f).coerceIn(0f, 1f)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFFFFB300).copy(alpha = 0.12f) else Color.Transparent)
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                .pointerInput(bandIdx) {
                                    detectDragGestures { _, dragAmount ->
                                        val delta = -dragAmount.y / 80f
                                        val newFrac = (fraction + delta).coerceIn(0f, 1f)
                                        val newLevel = ((newFrac * 3000) - 1500).toInt().toShort()
                                        onBandChange(bandIdx, newLevel)
                                    }
                                }
                        ) {
                            val db = (level / 100).toInt()
                            Text(
                                text = if (db > 0) "+$db" else "$db",
                                fontSize = 9.sp,
                                color = if (isSelected) Color(0xFFFFB300) else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Vertical bar track
                            Box(
                                modifier = Modifier
                                    .width(8.dp)
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF262626)),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(fraction)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelected) Color(0xFFFFB300) else accentColor)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = freqLabel,
                                fontSize = 9.sp,
                                color = if (isSelected) Color(0xFFFFB300) else Color(0xFF888888),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
