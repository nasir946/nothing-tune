package com.nothing.tune.data

import android.content.ContentUris
import android.net.Uri

enum class AudioQuality {
    HI_RES,
    LOSSLESS,
    STANDARD
}

enum class SortOption(val label: String) {
    ARTIST("Artist"),
    AUTHOR("Author"),
    ALBUM("Album"),
    YEAR("Year"),
    GENRE("Genre"),
    DATE_ADDED("Date"),
    SIZE("Size"),
    DURATION("Duration"),
    CUSTOM("Custom")
}

data class MusicTrack(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val uri: Uri,
    val path: String,
    val albumId: Long = 0,
    val format: String = "MP3",
    val sampleRate: Int = 44100,
    val bitDepth: Int = 16,
    val bitrateKbps: Int = 320,
    val quality: AudioQuality = AudioQuality.STANDARD,
    val isAudiobook: Boolean = false,
    val audiobookPositionMs: Long = 0,
    val isFavorite: Boolean = false,
    val onlineArtUrl: String? = null,
    val playCount: Int = 0,
    val lastPlayedTimestamp: Long = 0L,
    val year: Int = 0,
    val genre: String = "Unknown",
    val author: String = "",
    val dateAdded: Long = 0L,
    val sizeBytes: Long = 0L
) {
    val artworkUri: Uri
        get() = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)

    val effectiveAuthor: String
        get() = author.ifBlank { artist }
}
