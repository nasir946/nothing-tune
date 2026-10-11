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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.nothing.tune.data.MusicTrack
import com.nothing.tune.player.AudioPlayerController
import com.nothing.tune.ui.components.DotMatrixText
import com.nothing.tune.ui.components.NothingArtwork
import com.nothing.tune.ui.components.formatDuration
import com.nothing.tune.ui.theme.AccentRed
import com.nothing.tune.ui.theme.PureBlack

@Composable
fun AlbumDetailScreen(
    title: String,
    tracks: List<MusicTrack>,
    controller: AudioPlayerController,
    onBack: () -> Unit,
    onOpenNowPlaying: () -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalDurationMs = remember(tracks) { tracks.sumOf { it.durationMs } }
    val durationMin = totalDurationMs / 60000
    val firstTrack = tracks.firstOrNull()
    var isFavoritePlaylist by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        // ── Top Bar ────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            IconButton(onClick = { /* Menu */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = Color.White
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp)
        ) {
            // Header: Large Artwork & Album Details
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NothingArtwork(
                        track = firstTrack,
                        modifier = Modifier
                            .size(220.dp)
                            .clip(RoundedCornerShape(22.dp)),
                        cornerRadius = 22.dp,
                        seed = title.hashCode()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    DotMatrixText(
                        text = title.take(18),
                        dotSize = 2.6.dp,
                        dotSpacing = 1.3.dp,
                        activeColor = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = firstTrack?.artist ?: "Nothing Artist",
                        color = Color(0xFFAAAAAA),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${tracks.size} songs • $durationMin min",
                        color = Color(0xFF666666),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // Buttons Row: Play, Shuffle, Favorite
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play Pill Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(23.dp))
                                .background(Color.White)
                                .clickable {
                                    if (tracks.isNotEmpty()) {
                                        controller.playTrack(tracks.first(), tracks, 0)
                                        onOpenNowPlaying()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Play",
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Shuffle Pill Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(23.dp))
                                .background(Color(0xFF16161A))
                                .border(1.dp, Color(0xFF262628), RoundedCornerShape(23.dp))
                                .clickable {
                                    if (tracks.isNotEmpty()) {
                                        val shuffled = tracks.shuffled()
                                        controller.playTrack(shuffled.first(), shuffled, 0)
                                        onOpenNowPlaying()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Shuffle",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Favorite Button
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF16161A))
                                .border(1.dp, Color(0xFF262628), CircleShape)
                                .clickable { isFavoritePlaylist = !isFavoritePlaylist },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isFavoritePlaylist) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavoritePlaylist) AccentRed else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Tracklist Items
            itemsIndexed(tracks) { index, track ->
                var menuExpanded by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            controller.playTrack(track, tracks, index)
                            onOpenNowPlaying()
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}",
                        color = Color(0xFF888888),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(26.dp)
                    )

                    NothingArtwork(
                        track = track,
                        modifier = Modifier.size(42.dp),
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

                    Text(
                        text = formatDuration(track.durationMs),
                        color = Color(0xFF777777),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
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
                                text = { Text("Play Next", color = Color.White) },
                                onClick = {
                                    controller.addToQueue(track)
                                    menuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (track.isFavorite) "Remove Favorite" else "Add to Favorites",
                                        color = Color.White
                                    )
                                },
                                onClick = {
                                    onToggleFavorite(track.id, !track.isFavorite)
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
