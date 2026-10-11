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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.nothing.tune.data.MusicRepository
import com.nothing.tune.data.MusicTrack
import com.nothing.tune.player.AudioPlayerController
import com.nothing.tune.ui.components.NothingArtwork
import com.nothing.tune.ui.theme.PureBlack

@Composable
fun SearchScreen(
    tracks: List<MusicTrack>,
    controller: AudioPlayerController,
    repository: MusicRepository,
    onBack: () -> Unit,
    onOpenNowPlaying: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var recentSearches by remember { mutableStateOf(repository.getRecentSearches()) }
    val mostPlayed = remember(tracks) { repository.getMostPlayedTracks(tracks, limit = 8) }

    val filteredTracks = remember(query, tracks) {
        if (query.isBlank()) emptyList()
        else {
            tracks.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.artist.contains(query, ignoreCase = true) ||
                        it.album.contains(query, ignoreCase = true)
            }
        }
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
                text = "Search",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // ── Search Input Field ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    if (it.isNotBlank()) {
                        repository.addSearchQuery(it)
                    }
                },
                placeholder = {
                    Text(
                        text = "Search for songs, artists, albums...",
                        color = Color(0xFF777777),
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF888888),
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Color(0xFF888888),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(22.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF141416),
                    unfocusedContainerColor = Color(0xFF141416),
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color(0xFF262628),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Main Content Area ──────────────────────────────────────────────────
        if (query.isBlank()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp)
            ) {
                // Section 1: Recent Searches
                if (recentSearches.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Searches",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Clear all",
                                color = Color(0xFF888888),
                                fontSize = 12.sp,
                                modifier = Modifier.clickable {
                                    repository.clearSearchHistory()
                                    recentSearches = emptyList()
                                }
                            )
                        }
                    }

                    items(recentSearches) { term ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { query = term }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color(0xFF666666),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = term,
                                color = Color(0xFFCCCCCC),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                // Section 2: Popular Right Now / Most Played
                item {
                    Text(
                        text = "Popular Right Now",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }

                val popularList = if (mostPlayed.isNotEmpty()) mostPlayed else tracks.take(6)
                itemsIndexed(popularList) { index, track ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                controller.playTrack(track, popularList, index)
                                onOpenNowPlaying()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rank number
                        Text(
                            text = "${index + 1}",
                            color = Color(0xFF888888),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(24.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Thumbnail
                        NothingArtwork(
                            track = track,
                            modifier = Modifier.size(46.dp),
                            cornerRadius = 12.dp
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        // Title & Subtitle
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${track.artist} · ${if (track.playCount > 0) "${track.playCount} plays" else "Track"}",
                                color = Color(0xFF888888),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Play circle button
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E1E22))
                                .border(1.dp, Color(0xFF2E2E32), CircleShape)
                                .clickable {
                                    controller.playTrack(track, popularList, index)
                                    onOpenNowPlaying()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Live Search Results
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp)
            ) {
                if (filteredTracks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No results found for \"$query\"",
                                color = Color(0xFF777777),
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    itemsIndexed(filteredTracks) { index, track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    controller.playTrack(track, filteredTracks, index)
                                    onOpenNowPlaying()
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NothingArtwork(
                                track = track,
                                modifier = Modifier.size(46.dp),
                                cornerRadius = 12.dp
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${track.artist} · ${track.album}",
                                    color = Color(0xFF888888),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
