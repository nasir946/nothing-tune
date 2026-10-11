package com.nothing.tune.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.Player
import com.nothing.tune.player.AudioDeviceManager
import com.nothing.tune.player.AudioPlayerController
import com.nothing.tune.ui.components.DotMatrixText
import com.nothing.tune.ui.components.NothingArtwork
import com.nothing.tune.ui.components.VisualizerStyle
import com.nothing.tune.ui.components.WaveformVisualizer
import com.nothing.tune.ui.components.formatDuration
import com.nothing.tune.ui.theme.AccentRed
import com.nothing.tune.ui.theme.PureBlack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    controller: AudioPlayerController,
    audioDeviceManager: AudioDeviceManager,
    accentColor: Color,
    onClose: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenQueue: () -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by controller.uiState.collectAsState()
    val freqBands by controller.visualizer.frequencyBands.collectAsState()
    val track = uiState.currentTrack ?: return

    var showLyricsSheet by remember { mutableStateOf(false) }
    var showDeviceSheet by remember { mutableStateOf(false) }
    var showMoreSheet by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var topMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ── Top Bar ────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Collapse",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Text(
                text = "NOW PLAYING",
                color = Color(0xFFAAAAAA),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.5.sp
            )

            Box {
                IconButton(onClick = { topMenuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                DropdownMenu(
                    expanded = topMenuExpanded,
                    onDismissRequest = { topMenuExpanded = false },
                    modifier = Modifier.background(Color(0xFF1E1E22))
                ) {
                    DropdownMenuItem(
                        text = { Text("Playback Speed", color = Color.White) },
                        onClick = {
                            topMenuExpanded = false
                            showSpeedDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Equalizer", color = Color.White) },
                        onClick = {
                            topMenuExpanded = false
                            onOpenEqualizer()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Sleep Timer", color = Color.White) },
                        onClick = {
                            topMenuExpanded = false
                            onOpenSleepTimer()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("View Queue", color = Color.White) },
                        onClick = {
                            topMenuExpanded = false
                            onOpenQueue()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // ── Big Album Artwork ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            NothingArtwork(
                track = track,
                modifier = Modifier
                    .size(290.dp)
                    .clip(RoundedCornerShape(26.dp)),
                cornerRadius = 26.dp
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // ── Title & Artist Row with Heart Icon ─────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                DotMatrixText(
                    text = track.title.take(18),
                    dotSize = 2.8.dp,
                    dotSpacing = 1.3.dp,
                    activeColor = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${track.artist}.",
                    color = Color(0xFF888888),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = { onToggleFavorite(track.id, !track.isFavorite) }
            ) {
                Icon(
                    imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (track.isFavorite) AccentRed else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ── Prominent Waveform Visualizer ──────────────────────────────────────
        WaveformVisualizer(
            isPlaying = uiState.isPlaying,
            style = VisualizerStyle.BARS,
            accentColor = Color.White,
            magnitudes = freqBands,
            onStyleChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // ── Seek Bar & Time Stamps ─────────────────────────────────────────────
        val currentMs = uiState.currentPositionMs
        val durationMs = uiState.durationMs.coerceAtLeast(1L)
        val progress = (currentMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)

        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = progress,
                onValueChange = { frac ->
                    controller.seekTo((frac * durationMs).toLong())
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color(0xFF262628)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDuration(currentMs),
                    color = Color(0xFF777777),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatDuration(durationMs),
                    color = Color(0xFF777777),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Transport Playback Controls ────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle
            IconButton(onClick = { controller.toggleShuffle() }) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (uiState.isShuffle) Color.White else Color(0xFF555555),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Previous
            IconButton(
                onClick = { controller.skipPrevious() },
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Play / Pause Circle (Big White Button)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { controller.togglePlayPause() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                    tint = Color.Black,
                    modifier = Modifier.size(34.dp)
                )
            }

            // Next
            IconButton(
                onClick = { controller.skipNext() },
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Repeat
            IconButton(onClick = { controller.cycleRepeatMode() }) {
                val repeatActive = uiState.repeatMode != Player.REPEAT_MODE_OFF
                val icon = if (uiState.repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat
                Icon(
                    imageVector = icon,
                    contentDescription = "Repeat",
                    tint = if (repeatActive) Color.White else Color(0xFF555555),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // ── Bottom Action Pills Row ([Lyrics] [Device] [··· More]) ─────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lyrics Pill
            ActionPill(
                icon = Icons.AutoMirrored.Filled.Chat,
                label = "Lyrics",
                onClick = { showLyricsSheet = true }
            )

            // Device Pill
            ActionPill(
                icon = Icons.Default.Cast,
                label = "Device",
                onClick = { showDeviceSheet = true }
            )

            // More Pill
            ActionPill(
                icon = Icons.Default.MoreHoriz,
                label = "More",
                onClick = { showMoreSheet = true }
            )
        }
    }

    // ── Lyrics Bottom Sheet ────────────────────────────────────────────────────
    if (showLyricsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLyricsSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color(0xFF141416)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LYRICS",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "${track.title}\n${track.artist}\n\n[Instrumental & Lyrics Passthrough]\n\n" +
                            "Feel the sound\n" +
                            "Lost in the quiet frequency\n" +
                            "Pure bit-perfect lossless flow\n" +
                            "Minimal. Focused. Beautifully simple.\n\n" +
                            "♪ ♪ ♪",
                    color = Color(0xFFCCCCCC),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // ── Connected Device Modal Sheet ───────────────────────────────────────────
    if (showDeviceSheet) {
        val context = LocalContext.current
        val connectedDevices by audioDeviceManager.devices.collectAsState()

        ModalBottomSheet(
            onDismissRequest = { showDeviceSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color(0xFF141416)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "AUDIO OUTPUT & BLUETOOTH",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                // List real detected connected devices
                connectedDevices.forEach { dev ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1E1E22))
                            .border(1.dp, Color(0xFF2E2E32), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cast,
                            contentDescription = null,
                            tint = if (dev.isBluetooth) AccentRed else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = dev.name,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${dev.typeName} • Connected",
                                color = Color(0xFF888888),
                                fontSize = 12.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Working Dual Audio / Multi-Device Play Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .clickable {
                            (context as? Activity)?.let { act ->
                                audioDeviceManager.openSystemMediaOutputSwitcher(act)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Dual Audio / Multi-Device Play",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // ── More Bottom Sheet ──────────────────────────────────────────────────────
    if (showMoreSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color(0xFF141416)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "MORE CONTROLS",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(18.dp))

                // Equalizer row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E1E22))
                        .clickable {
                            showMoreSheet = false
                            onOpenEqualizer()
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Equalizer & Audio FX",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sleep Timer row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E1E22))
                        .clickable {
                            showMoreSheet = false
                            onOpenSleepTimer()
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Nightlight,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Sleep Timer",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Playback Speed row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E1E22))
                        .clickable {
                            showMoreSheet = false
                            showSpeedDialog = true
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Playback Speed (${uiState.playbackSpeed}x)",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // ── Speed Dialog ───────────────────────────────────────────────────────────
    if (showSpeedDialog) {
        val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = { Text("Playback Speed", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = Color(0xFF1E1E22),
            text = {
                Column {
                    speeds.forEach { speed ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    controller.setPlaybackSpeed(speed)
                                    showSpeedDialog = false
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${speed}x",
                                color = if (uiState.playbackSpeed == speed) Color.White else Color(0xFF888888),
                                fontSize = 15.sp,
                                fontWeight = if (uiState.playbackSpeed == speed) FontWeight.Bold else FontWeight.Normal
                            )
                            if (uiState.playbackSpeed == speed) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun ActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF141416))
            .border(1.dp, Color(0xFF262628), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFFBBBBBB),
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = Color(0xFFDDDDDD),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
