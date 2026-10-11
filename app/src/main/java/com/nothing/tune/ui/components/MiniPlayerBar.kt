package com.nothing.tune.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.tune.data.MusicTrack

@Composable
fun MiniPlayerBar(
    track: MusicTrack?,
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
    onClick: () -> Unit,
    onOpenQueue: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (track == null) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF16161A))
            .border(1.dp, Color(0xFF262628), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artwork Thumbnail
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                NothingArtwork(
                    track = track,
                    modifier = Modifier.size(46.dp),
                    cornerRadius = 12.dp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title and Artist
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = track.artist,
                    color = Color(0xFF888888),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Play/Pause Button
            IconButton(
                onClick = onTogglePlayPause,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Queue Button
            IconButton(
                onClick = onOpenQueue,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = "Queue",
                    tint = Color(0xFFAAAAAA),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
