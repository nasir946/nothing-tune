package com.nothing.tune.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nothing.tune.data.AudioQuality
import com.nothing.tune.data.MusicTrack
import com.nothing.tune.player.AudioPlayerController
import com.nothing.tune.ui.components.formatDuration

@Composable
fun LibraryScreen(
    tracks: List<MusicTrack>,
    controller: AudioPlayerController,
    appName: String,
    accentColor: Color,
    onOpenNowPlaying: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStreaming: () -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    onToggleAudiobook: (Long, Boolean) -> Unit
) {
    val uiState by controller.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var qualityFilter by remember { mutableStateOf<AudioQuality?>(null) }
    var onlyAudiobooks by remember { mutableStateOf(false) }

    val tabs = listOf("Songs", "Albums", "Artists", "Audiobooks")

    // Filter tracks
    val filteredTracks = tracks.filter { track ->
        val matchesSearch = searchQuery.isBlank() ||
                track.title.contains(searchQuery, ignoreCase = true) ||
                track.artist.contains(searchQuery, ignoreCase = true) ||
                track.album.contains(searchQuery, ignoreCase = true)

        val matchesQuality = qualityFilter == null || track.quality == qualityFilter
        val matchesAudiobook = !onlyAudiobooks || track.isAudiobook

        matchesSearch && matchesQuality && matchesAudiobook
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = appName.uppercase(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = accentColor,
                letterSpacing = 2.sp
            )
            Row {
                IconButton(onClick = onOpenStreaming) {
                    Icon(Icons.Default.CloudDownload, contentDescription = "Stream & Download", tint = Color.White)
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Search title, artist, album...", color = Color.Gray, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = Color(0xFF262626),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips (Quality & Audiobook)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = qualityFilter == null && !onlyAudiobooks,
                onClick = { qualityFilter = null; onlyAudiobooks = false },
                label = { Text("All", fontSize = 11.sp) }
            )
            FilterChip(
                selected = qualityFilter == AudioQuality.HI_RES,
                onClick = { qualityFilter = if (qualityFilter == AudioQuality.HI_RES) null else AudioQuality.HI_RES },
                label = { Text("Hi-Res", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFFB300),
                    selectedLabelColor = Color.Black
                )
            )
            FilterChip(
                selected = qualityFilter == AudioQuality.LOSSLESS,
                onClick = { qualityFilter = if (qualityFilter == AudioQuality.LOSSLESS) null else AudioQuality.LOSSLESS },
                label = { Text("Lossless", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF81C784),
                    selectedLabelColor = Color.Black
                )
            )
            FilterChip(
                selected = onlyAudiobooks,
                onClick = { onlyAudiobooks = !onlyAudiobooks },
                label = { Text("Books", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF64B5F6),
                    selectedLabelColor = Color.Black
                )
            )
        }

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Black,
            contentColor = accentColor,
            edgePadding = 16.dp,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        selectedTab = index
                        if (index == 3) onlyAudiobooks = true
                    },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) accentColor else Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0, 3 -> {
                    // Songs list / Audiobooks
                    val displayList = if (selectedTab == 3) filteredTracks.filter { it.isAudiobook } else filteredTracks
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(displayList) { index, track ->
                            TrackListItem(
                                track = track,
                                isPlaying = uiState.currentTrack?.id == track.id && uiState.isPlaying,
                                accentColor = accentColor,
                                onClick = { controller.playTrack(track, displayList, index) },
                                onToggleFavorite = { onToggleFavorite(track.id, !track.isFavorite) },
                                onToggleAudiobook = { onToggleAudiobook(track.id, !track.isAudiobook) }
                            )
                        }
                    }
                }

                1 -> {
                    // Albums grid - Responsive for One UI Freeform & Tablets
                    val albums = filteredTracks.groupBy { it.album }
                    BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        val columns = (maxWidth / 150.dp).toInt().coerceIn(2, 6)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(albums.keys.toList()) { albumTitle ->
                                val albumTracks = albums[albumTitle] ?: emptyList()
                                val first = albumTracks.firstOrNull()

                                Column(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            if (albumTracks.isNotEmpty()) {
                                                controller.playTrack(albumTracks.first(), albumTracks, 0)
                                            }
                                        }
                                        .padding(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF1E1E22)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = albumTitle,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${first?.artist ?: ""} · ${albumTracks.size} tracks",
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Artists list
                    val artists = filteredTracks.groupBy { it.artist }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(artists.keys.toList()) { artistName ->
                            val artistTracks = artists[artistName] ?: emptyList()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (artistTracks.isNotEmpty()) {
                                            controller.playTrack(artistTracks.first(), artistTracks, 0)
                                        }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(22.dp))
                                        .background(Color(0xFF222226)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = artistName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                        color = accentColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(text = artistName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "${artistTracks.size} songs", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Persistent Mini Player bar
        if (uiState.currentTrack != null) {
            MiniPlayerBar(
                track = uiState.currentTrack!!,
                isPlaying = uiState.isPlaying,
                accentColor = accentColor,
                onPlayPause = { controller.togglePlayPause() },
                onSkipNext = { controller.skipNext() },
                onSkipPrev = { controller.skipPrevious() },
                onClick = onOpenNowPlaying
            )
        }
    }
}

@Composable
fun TrackListItem(
    track: MusicTrack,
    isPlaying: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleAudiobook: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E1E22)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.MusicNote, contentDescription = null, tint = if (isPlaying) accentColor else Color.Gray)
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = track.title,
                    color = if (isPlaying) accentColor else Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                // Quality Badge
                if (track.quality == AudioQuality.HI_RES) {
                    Spacer(modifier = Modifier.width(6.dp))
                    QualityBadge("HI-RES", Color(0xFFFFB300))
                } else if (track.quality == AudioQuality.LOSSLESS) {
                    Spacer(modifier = Modifier.width(6.dp))
                    QualityBadge("LOSSLESS", Color(0xFF81C784))
                }

                if (track.isAudiobook) {
                    Spacer(modifier = Modifier.width(6.dp))
                    QualityBadge("BOOK", Color(0xFF64B5F6))
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${track.artist} · ${formatDuration(track.durationMs)}",
                color = Color.Gray,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // More actions
        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.Gray)
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.background(Color(0xFF1E1E22))
            ) {
                DropdownMenuItem(
                    text = { Text(if (track.isFavorite) "Remove Favorite" else "Add to Favorites", color = Color.White) },
                    leadingIcon = { Icon(if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null, tint = Color.Red) },
                    onClick = {
                        onToggleFavorite()
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(if (track.isAudiobook) "Unmark Audiobook" else "Mark as Audiobook", color = Color.White) },
                    leadingIcon = { Icon(if (track.isAudiobook) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, contentDescription = null, tint = Color(0xFF64B5F6)) },
                    onClick = {
                        onToggleAudiobook()
                        menuExpanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun QualityBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(text = label, color = color, fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MiniPlayerBar(
    track: MusicTrack,
    isPlaying: Boolean,
    accentColor: Color,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrev: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color(0xFF141416))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF222226)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.MusicNote, contentDescription = null, tint = accentColor)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                color = Color.Gray,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onSkipPrev) {
            Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = Color.White)
        }
        IconButton(onClick = onPlayPause) {
            Icon(
                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Play/Pause",
                tint = accentColor
            )
        }
        IconButton(onClick = onSkipNext) {
            Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White)
        }
    }
}
