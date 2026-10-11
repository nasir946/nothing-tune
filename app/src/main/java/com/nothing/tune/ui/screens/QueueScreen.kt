package com.nothing.tune.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.tune.player.AudioPlayerController
import com.nothing.tune.ui.components.NothingArtwork
import com.nothing.tune.ui.theme.PureBlack

@Composable
fun QueueScreen(
    controller: AudioPlayerController,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by controller.uiState.collectAsState()
    val currentTrack = uiState.currentTrack
    val queue = uiState.queue
    val currentQueueIndex = uiState.queueIndex

    val upNextList = remember(queue, currentQueueIndex) {
        if (queue.isEmpty()) emptyList()
        else queue.filterIndexed { index, _ -> index > currentQueueIndex }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        // ── Top Bar ────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
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
                text = "Queue",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp)
        ) {
            // ── Now Playing Section ────────────────────────────────────────────
            item {
                Text(
                    text = "Now Playing",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp, bottom = 12.dp)
                )

                if (currentTrack != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF16161A))
                            .border(1.dp, Color(0xFF262628), RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NothingArtwork(
                            track = currentTrack,
                            modifier = Modifier.size(46.dp),
                            cornerRadius = 10.dp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentTrack.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentTrack.artist,
                                color = Color(0xFF888888),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Active audio equalizer glyph
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = "Playing",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // ── Up Next Section ────────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(22.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Up Next",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Clear",
                        color = Color(0xFF888888),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { controller.clearQueue() }
                    )
                }
            }

            if (upNextList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tracks up next in queue",
                            color = Color(0xFF666666),
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                itemsIndexed(upNextList) { relIndex, track ->
                    val actualIndex = currentQueueIndex + 1 + relIndex
                    var menuExpanded by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { controller.playQueueIndex(actualIndex) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Drag handle
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = "Reorder",
                            tint = Color(0xFF555555),
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        NothingArtwork(
                            track = track,
                            modifier = Modifier.size(44.dp),
                            cornerRadius = 10.dp
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = track.artist,
                                color = Color(0xFF888888),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Box {
                            IconButton(
                                onClick = { menuExpanded = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    tint = Color(0xFF666666),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                modifier = Modifier.background(Color(0xFF1E1E22))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Play Now", color = Color.White) },
                                    onClick = {
                                        controller.playQueueIndex(actualIndex)
                                        menuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Remove from Queue", color = Color.White) },
                                    onClick = {
                                        controller.removeFromQueue(actualIndex)
                                        menuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
