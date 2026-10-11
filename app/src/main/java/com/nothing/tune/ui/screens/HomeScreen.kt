package com.nothing.tune.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.tune.data.AudioQuality
import com.nothing.tune.data.MusicRepository
import com.nothing.tune.data.MusicTrack
import com.nothing.tune.player.AudioPlayerController
import com.nothing.tune.ui.components.DotMatrixLogo
import com.nothing.tune.ui.components.DotMatrixText
import com.nothing.tune.ui.components.FilterSortSheet
import com.nothing.tune.ui.components.FilterSortState
import com.nothing.tune.ui.components.NothingArtwork
import com.nothing.tune.ui.components.formatDuration
import com.nothing.tune.ui.theme.AccentRed
import com.nothing.tune.ui.theme.PureBlack

@Composable
fun HomeScreen(
    tracks: List<MusicTrack>,
    controller: AudioPlayerController,
    repository: MusicRepository,
    onOpenNowPlaying: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStreaming: () -> Unit,
    onOpenPlaylistDetail: (title: String, playlistTracks: List<MusicTrack>) -> Unit,
    onToggleFavorite: (trackId: Long, isFav: Boolean) -> Unit,
    onToggleAudiobook: (trackId: Long, isBook: Boolean) -> Unit,
    onHideTrack: (trackId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("For You", "Audiobooks", "Playlists", "Artists", "Albums", "Radio & Stream")

    var showFilterSortSheet by remember { mutableStateOf(false) }
    var filterSortState by remember { mutableStateOf(FilterSortState()) }
    var trackToHide by remember { mutableStateOf<MusicTrack?>(null) }

    // Apply active filter and sort
    val processedTracks = remember(tracks, filterSortState) {
        var list = tracks

        if (filterSortState.hiResOnly) {
            list = list.filter { it.quality == AudioQuality.HI_RES }
        }
        if (filterSortState.losslessOnly) {
            list = list.filter { it.quality == AudioQuality.LOSSLESS || it.quality == AudioQuality.HI_RES }
        }
        if (filterSortState.audiobooksOnly) {
            list = list.filter { it.isAudiobook }
        }

        repository.sortTracks(list, filterSortState.sortOption, filterSortState.ascending)
    }

    val recentlyPlayed = remember(processedTracks) {
        repository.getRecentlyPlayedTracks(processedTracks, limit = 10)
    }

    val mostPlayed = remember(processedTracks) {
        repository.getMostPlayedTracks(processedTracks, limit = 10)
    }

    val mixes = remember(processedTracks) {
        repository.getCuratedMixes(processedTracks)
    }

    val audiobookTracks = remember(tracks) {
        tracks.filter { it.isAudiobook }
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
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DotMatrixLogo(
                dotSize = 3.6.dp,
                dotSpacing = 1.6.dp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenSearch) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // ── Category Pills Row with Filter Tune Button ─────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category pills horizontal scroll
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
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
                                if (title == "Radio & Stream") {
                                    onOpenStreaming()
                                } else {
                                    selectedTab = index
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) Color.Black else Color(0xFF999999),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Filter / Tune button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF16161A))
                    .border(1.dp, Color(0xFF262628), CircleShape)
                    .clickable { showFilterSortSheet = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Filter & Sort",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Tab Content ────────────────────────────────────────────────────────
        when (selectedTab) {
            0 -> ForYouContent(
                tracks = processedTracks,
                recentlyPlayed = recentlyPlayed,
                mixes = mixes,
                onPlayTrack = { track, queue ->
                    controller.playTrack(track, queue)
                    onOpenNowPlaying()
                },
                onOpenPlaylist = onOpenPlaylistDetail,
                onSeeAllRecentlyPlayed = {
                    onOpenPlaylistDetail("Recently Played", recentlyPlayed)
                },
                onSeeAllMadeForYou = {
                    onOpenPlaylistDetail("Most Played Mix", mostPlayed)
                },
                onToggleFavorite = onToggleFavorite,
                onToggleAudiobook = onToggleAudiobook,
                onHideTrack = { trackToHide = it },
                onAddToQueue = { controller.addToQueue(it) }
            )

            1 -> AudiobooksContent(
                audiobooks = audiobookTracks,
                controller = controller,
                onPlayBook = { track ->
                    controller.playTrack(track)
                    onOpenNowPlaying()
                },
                onToggleAudiobook = onToggleAudiobook,
                onHideTrack = { trackToHide = it }
            )

            2 -> PlaylistsContent(
                tracks = processedTracks,
                recentlyPlayed = recentlyPlayed,
                mostPlayed = mostPlayed,
                onOpenPlaylist = onOpenPlaylistDetail
            )

            3 -> ArtistsContent(
                tracks = processedTracks,
                onOpenArtist = { artistName, artistTracks ->
                    onOpenPlaylistDetail(artistName, artistTracks)
                }
            )

            4 -> AlbumsContent(
                tracks = processedTracks,
                onOpenAlbum = { albumName, albumTracks ->
                    onOpenPlaylistDetail(albumName, albumTracks)
                }
            )
        }
    }

    // ── Filter & Sort Sheet ────────────────────────────────────────────────────
    if (showFilterSortSheet) {
        FilterSortSheet(
            currentState = filterSortState,
            onDismiss = { showFilterSortSheet = false },
            onApply = { filterSortState = it }
        )
    }

    // ── Confirmation Dialog to Remove/Hide Track ───────────────────────────────
    trackToHide?.let { trk ->
        AlertDialog(
            onDismissRequest = { trackToHide = null },
            title = { Text("Remove from Library", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = Color(0xFF1E1E22),
            text = {
                Text(
                    text = "Hide \"${trk.title}\" from your library list?\n\n(The audio file will not be deleted from your phone).",
                    color = Color(0xFFCCCCCC),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onHideTrack(trk.id)
                        trackToHide = null
                    }
                ) {
                    Text("Remove", color = AccentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { trackToHide = null }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

// ── FOR YOU TAB CONTENT ────────────────────────────────────────────────────────

@Composable
private fun ForYouContent(
    tracks: List<MusicTrack>,
    recentlyPlayed: List<MusicTrack>,
    mixes: Map<String, List<MusicTrack>>,
    onPlayTrack: (MusicTrack, List<MusicTrack>) -> Unit,
    onOpenPlaylist: (String, List<MusicTrack>) -> Unit,
    onSeeAllRecentlyPlayed: () -> Unit,
    onSeeAllMadeForYou: () -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    onToggleAudiobook: (Long, Boolean) -> Unit,
    onHideTrack: (MusicTrack) -> Unit,
    onAddToQueue: (MusicTrack) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp)
    ) {
        // 1. "Today's Mix" Hero Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .height(150.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF16161A))
                    .border(1.dp, Color(0xFF262628), RoundedCornerShape(22.dp))
                    .clickable {
                        val mixTracks = mixes["Today's Mix"] ?: tracks
                        if (mixTracks.isNotEmpty()) {
                            onPlayTrack(mixTracks.first(), mixTracks)
                        }
                    }
            ) {
                NothingArtwork(
                    track = tracks.firstOrNull(),
                    modifier = Modifier.fillMaxSize(),
                    seed = 101,
                    cornerRadius = 22.dp
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.85f), Color.Black.copy(alpha = 0.35f))
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(22.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        DotMatrixText(
                            text = "Today's",
                            dotSize = 2.4.dp,
                            dotSpacing = 1.2.dp,
                            activeColor = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        DotMatrixText(
                            text = "Mix",
                            dotSize = 2.4.dp,
                            dotSpacing = 1.2.dp,
                            activeColor = Color.White
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Chill. Focus. Repeat.",
                            color = Color(0xFFAAAAAA),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Play Mix",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 2. "Recently Played" Section
        item {
            SectionHeader(
                title = "Recently Played",
                onSeeAll = onSeeAllRecentlyPlayed
            )

            val displayList = if (recentlyPlayed.isNotEmpty()) recentlyPlayed else tracks.take(6)

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(displayList) { track ->
                    var menuExpanded by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .width(115.dp)
                            .clickable { onPlayTrack(track, displayList) }
                    ) {
                        Box {
                            NothingArtwork(
                                track = track,
                                modifier = Modifier
                                    .size(115.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                cornerRadius = 16.dp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = track.title,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist,
                            color = Color(0xFF888888),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 3. "Made for You" Section
        item {
            Spacer(modifier = Modifier.height(18.dp))
            SectionHeader(
                title = "Made for You",
                onSeeAll = onSeeAllMadeForYou
            )

            val madeForYouCategories = listOf(
                Pair("Deep Focus", mixes["Deep Focus"] ?: tracks),
                Pair("Lo-Fi Room", mixes["Lo-Fi Room"] ?: tracks),
                Pair("Workout", mixes["Workout"] ?: tracks)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(madeForYouCategories) { (name, mixTracks) ->
                    Column(
                        modifier = Modifier
                            .width(115.dp)
                            .clickable { onOpenPlaylist(name, mixTracks) }
                    ) {
                        NothingArtwork(
                            track = mixTracks.firstOrNull(),
                            modifier = Modifier
                                .size(115.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            cornerRadius = 16.dp,
                            seed = name.hashCode()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = name,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Playlist",
                            color = Color(0xFF888888),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 4. "All Tracks" Section with 3-Dot Menus
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "All Tracks (${tracks.size})",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 10.dp)
            )
        }

        itemsIndexed(tracks) { index, track ->
            var menuExpanded by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlayTrack(track, tracks) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NothingArtwork(
                    track = track,
                    modifier = Modifier.size(44.dp),
                    cornerRadius = 10.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = track.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (track.isAudiobook) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BOOK",
                                color = AccentRed,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AccentRed.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${track.artist} · ${formatDuration(track.durationMs)}",
                        color = Color(0xFF888888),
                        fontSize = 11.sp,
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
                            text = { Text("Play Next", color = Color.White) },
                            onClick = {
                                onAddToQueue(track)
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (track.isFavorite) "Remove from Favorites" else "Add to Favorites",
                                    color = Color.White
                                )
                            },
                            onClick = {
                                onToggleFavorite(track.id, !track.isFavorite)
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (track.isAudiobook) "Remove Audiobook Mark" else "Mark / Add as Book",
                                    color = Color.White
                                )
                            },
                            onClick = {
                                onToggleAudiobook(track.id, !track.isAudiobook)
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Remove from Library", color = AccentRed) },
                            onClick = {
                                menuExpanded = false
                                onHideTrack(track)
                            }
                        )
                    }
                }
            }
        }
    }
}

// ── AUDIOBOOKS TAB CONTENT ─────────────────────────────────────────────────────

@Composable
private fun AudiobooksContent(
    audiobooks: List<MusicTrack>,
    controller: AudioPlayerController,
    onPlayBook: (MusicTrack) -> Unit,
    onToggleAudiobook: (Long, Boolean) -> Unit,
    onHideTrack: (MusicTrack) -> Unit
) {
    if (audiobooks.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = Color(0xFF555555),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "No Audiobooks Added Yet",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tap the 3 dots on any track and select \"Mark / Add as Book\" to add it here with bookmark progress.",
                    color = Color(0xFF888888),
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp)
        ) {
            item {
                Text(
                    text = "Audiobooks & Spoken Audio (${audiobooks.size})",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            items(audiobooks) { book ->
                var menuExpanded by remember { mutableStateOf(false) }
                val posMs = book.audiobookPositionMs
                val durMs = book.durationMs.coerceAtLeast(1L)
                val progress = (posMs.toFloat() / durMs.toFloat()).coerceIn(0f, 1f)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF16161A))
                        .border(1.dp, Color(0xFF262628), RoundedCornerShape(16.dp))
                        .clickable { onPlayBook(book) }
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NothingArtwork(
                            track = book,
                            modifier = Modifier.size(52.dp),
                            cornerRadius = 12.dp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = book.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Author: ${book.effectiveAuthor}",
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
                                    tint = Color(0xFF888888),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                modifier = Modifier.background(Color(0xFF1E1E22))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Remove from Books", color = Color.White) },
                                    onClick = {
                                        onToggleAudiobook(book.id, false)
                                        menuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Hide Track", color = AccentRed) },
                                    onClick = {
                                        menuExpanded = false
                                        onHideTrack(book)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape),
                        color = Color.White,
                        trackColor = Color(0xFF2A2A2E)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (posMs > 0) "Resume at ${formatDuration(posMs)} / ${formatDuration(durMs)}" else "Not started • ${formatDuration(durMs)}",
                            color = Color(0xFF777777),
                            fontSize = 11.sp
                        )

                        // Quick +/- Rewind/Forward Controls
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FastRewind,
                                contentDescription = "-10s",
                                tint = Color(0xFFAAAAAA),
                                modifier = Modifier
                                    .size(22.dp)
                                    .clickable {
                                        controller.seekTo((controller.uiState.value.currentPositionMs - 10000).coerceAtLeast(0L))
                                    }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = "+30s",
                                tint = Color(0xFFAAAAAA),
                                modifier = Modifier
                                    .size(22.dp)
                                    .clickable {
                                        controller.seekTo(controller.uiState.value.currentPositionMs + 30000)
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── PLAYLISTS TAB CONTENT ──────────────────────────────────────────────────────

@Composable
private fun PlaylistsContent(
    tracks: List<MusicTrack>,
    recentlyPlayed: List<MusicTrack>,
    mostPlayed: List<MusicTrack>,
    onOpenPlaylist: (String, List<MusicTrack>) -> Unit
) {
    val playlists = listOf(
        Pair("Favorites", tracks.filter { it.isFavorite }.ifEmpty { tracks.take(5) }),
        Pair("Most Played", mostPlayed),
        Pair("Recently Played", recentlyPlayed),
        Pair("Hi-Res Collection", tracks.filter { it.bitDepth >= 24 }.ifEmpty { tracks.take(6) }),
        Pair("Audiobooks & Podcasts", tracks.filter { it.isAudiobook }.ifEmpty { emptyList() })
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp)
    ) {
        items(playlists) { (title, pTracks) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF141416))
                    .border(1.dp, Color(0xFF242426), RoundedCornerShape(14.dp))
                    .clickable { onOpenPlaylist(title, pTracks) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NothingArtwork(
                    track = pTracks.firstOrNull(),
                    modifier = Modifier.size(54.dp),
                    cornerRadius = 12.dp,
                    seed = title.hashCode()
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${pTracks.size} tracks",
                        color = Color(0xFF888888),
                        fontSize = 12.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF666666)
                )
            }
        }
    }
}

// ── ARTISTS TAB CONTENT ────────────────────────────────────────────────────────

@Composable
private fun ArtistsContent(
    tracks: List<MusicTrack>,
    onOpenArtist: (String, List<MusicTrack>) -> Unit
) {
    val artistGroups = remember(tracks) {
        tracks.groupBy { it.artist }.toList().sortedByDescending { it.second.size }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp)
    ) {
        items(artistGroups) { (artist, aTracks) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onOpenArtist(artist, aTracks) }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NothingArtwork(
                    track = aTracks.firstOrNull(),
                    modifier = Modifier.size(50.dp),
                    isCircle = true,
                    seed = artist.hashCode()
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = artist,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${aTracks.size} songs",
                        color = Color(0xFF888888),
                        fontSize = 12.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF666666)
                )
            }
        }
    }
}

// ── ALBUMS TAB CONTENT ─────────────────────────────────────────────────────────

@Composable
private fun AlbumsContent(
    tracks: List<MusicTrack>,
    onOpenAlbum: (String, List<MusicTrack>) -> Unit
) {
    val albumGroups = remember(tracks) {
        tracks.groupBy { it.album }.toList().sortedByDescending { it.second.size }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp)
    ) {
        items(albumGroups) { (album, albTracks) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF141416))
                    .border(1.dp, Color(0xFF242426), RoundedCornerShape(14.dp))
                    .clickable { onOpenAlbum(album, albTracks) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NothingArtwork(
                    track = albTracks.firstOrNull(),
                    modifier = Modifier.size(54.dp),
                    cornerRadius = 12.dp
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = album,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${albTracks.firstOrNull()?.artist ?: ""} · ${albTracks.size} tracks",
                        color = Color(0xFF888888),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF666666)
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    onSeeAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "See all",
            color = Color(0xFF888888),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onSeeAll() }
        )
    }
}
