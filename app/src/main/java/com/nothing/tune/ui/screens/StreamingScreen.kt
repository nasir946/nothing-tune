package com.nothing.tune.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nothing.tune.network.ExtractedAudioMedia
import com.nothing.tune.network.OnlineMediaExtractor
import com.nothing.tune.player.AudioPlayerController
import com.nothing.tune.ui.components.DotMatrixText
import com.nothing.tune.ui.theme.AccentRed
import com.nothing.tune.ui.theme.PureBlack
import kotlinx.coroutines.launch

data class RadioStation(val name: String, val genre: String, val streamUrl: String)

@Composable
fun StreamingScreen(
    controller: AudioPlayerController,
    accentColor: Color,
    onBack: () -> Unit,
    onTrackStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var urlInput by remember { mutableStateOf("") }
    var isExtracting by remember { mutableStateOf(false) }
    var extractedMedia by remember { mutableStateOf<ExtractedAudioMedia?>(null) }
    var extractError by remember { mutableStateOf<String?>(null) }

    val presetStations = listOf(
        RadioStation("Lofi Girl Live", "Lofi / Study", "https://stream.zeno.fm/f3wvbbqmdg8uv"),
        RadioStation("Chillhop Radio", "Beats / Focus", "https://streams.ilovemusic.de/iloveradio17.mp3"),
        RadioStation("Classic Rock Hits", "Rock", "https://stream.revma.ihrhls.com/zc1465"),
        RadioStation("BBC Radio 1", "Pop / Hits", "https://stream.live.vc.bbcmedia.co.uk/bbc_radio_one"),
        RadioStation("Smooth Jazz Lounge", "Jazz", "https://streaming.exclusive.radio/er/smoothjazz/icecast.audio"),
        RadioStation("Synthwave / Retro", "Electronic", "https://stream.nightride.fm/nightride.m4a")
    )

    fun handleExtract() {
        if (urlInput.isBlank()) return
        isExtracting = true
        extractError = null
        extractedMedia = null

        scope.launch {
            val result = OnlineMediaExtractor.inspectAndExtract(urlInput)
            isExtracting = false
            result.onSuccess { media ->
                extractedMedia = media
            }.onFailure { err ->
                extractError = err.localizedMessage ?: "Failed to resolve media stream"
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        // ── Top Bar ────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            DotMatrixText(
                text = "STREAM & DOWNLOAD",
                dotSize = 2.4.dp,
                dotSpacing = 1.2.dp,
                activeColor = Color.White
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Section 1: Online Link Downloader & Streamer ───────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF141416))
                        .border(1.dp, Color(0xFF262628), RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Text(
                        text = "ONLINE AUDIO DOWNLOADER",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Paste any YouTube, SoundCloud, or Direct Audio URL:",
                        color = Color(0xFF888888),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "https://youtube.com/watch?v=... or soundcloud.com/...",
                                color = Color(0xFF666666),
                                fontSize = 12.sp
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color(0xFF262628),
                            focusedContainerColor = Color(0xFF1A1A1E),
                            unfocusedContainerColor = Color(0xFF1A1A1E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Inspect / Extract Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(23.dp))
                            .background(Color.White)
                            .clickable(enabled = !isExtracting) { handleExtract() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isExtracting) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Inspect & Resolve Link",
                                    color = Color.Black,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Extracted Result Card
                    extractedMedia?.let { media ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1E1E22))
                                .border(1.dp, Color(0xFF333336), RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            if (!media.thumbnailUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = media.thumbnailUrl,
                                    contentDescription = media.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = media.platform.uppercase(),
                                    color = AccentRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AccentRed.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = media.artist,
                                    color = Color(0xFF888888),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = media.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Dual Action Buttons: Stream & Download
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Stream Button
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(21.dp))
                                        .background(Color.White)
                                        .clickable {
                                            controller.playStreamUrl(media.streamUrl, media.title)
                                            onTrackStarted()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Stream",
                                            color = Color.Black,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Download Button
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(21.dp))
                                        .background(Color(0xFF262628))
                                        .border(1.dp, Color(0xFF3A3A3E), RoundedCornerShape(21.dp))
                                        .clickable {
                                            try {
                                                OnlineMediaExtractor.startDownload(context, media)
                                                Toast.makeText(context, "Download started: ${media.title}", Toast.LENGTH_LONG).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Download error: ${e.message}", Toast.LENGTH_LONG).show()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Download",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    extractError?.let { err ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Error: $err",
                            color = AccentRed,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // ── Section 2: Live Internet Radio Stations ───────────────────────
            item {
                Text(
                    text = "CURATED INTERNET RADIO",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(presetStations) { station ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF141416))
                        .border(1.dp, Color(0xFF242426), RoundedCornerShape(14.dp))
                        .clickable {
                            controller.playStreamUrl(station.streamUrl, station.name)
                            onTrackStarted()
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E1E22)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radio,
                            contentDescription = null,
                            tint = AccentRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = station.name,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = station.genre,
                            color = Color(0xFF888888),
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
