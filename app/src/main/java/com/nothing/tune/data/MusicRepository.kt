package com.nothing.tune.data

import android.content.ContentUris
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color as AndroidColor
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("nothing_tune_prefs", Context.MODE_PRIVATE)

    suspend fun loadTracks(): List<MusicTrack> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<MusicTrack>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.COMPOSER
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 OR ${MediaStore.Audio.Media.DURATION} > 5000"
        val hiddenIds = getHiddenTrackIds()

        try {
            context.contentResolver.query(
                uri, projection, selection, null,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val yearCol = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)
                val dateAddedCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
                val sizeCol = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
                val composerCol = cursor.getColumnIndex(MediaStore.Audio.Media.COMPOSER)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    if (hiddenIds.contains(id)) continue // Skip hidden tracks

                    val title = cursor.getString(titleCol) ?: "Unknown"
                    val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                    val album = cursor.getString(albumCol) ?: "Unknown Album"
                    val duration = cursor.getLong(durCol)
                    val path = if (dataCol >= 0) cursor.getString(dataCol) ?: "" else ""
                    val albumId = cursor.getLong(albumIdCol)

                    val year = if (yearCol >= 0) cursor.getInt(yearCol) else 0
                    val dateAdded = if (dateAddedCol >= 0) cursor.getLong(dateAddedCol) else 0L
                    val sizeBytes = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L
                    val author = if (composerCol >= 0) cursor.getString(composerCol) ?: "" else ""

                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                    val ext = if (path.contains('.')) path.substringAfterLast('.').uppercase() else "MP3"
                    val sampleRate = if (ext in listOf("FLAC", "WAV", "ALAC")) 96000 else 44100
                    val bitDepth = if (ext in listOf("FLAC", "WAV", "ALAC")) 24 else 16

                    val quality = when {
                        bitDepth >= 24 || sampleRate > 48000 -> AudioQuality.HI_RES
                        ext in listOf("FLAC", "ALAC", "WAV", "AIFF", "APE") -> AudioQuality.LOSSLESS
                        else -> AudioQuality.STANDARD
                    }

                    val isAudiobook = prefs.getBoolean("audiobook_$id", false) ||
                            title.contains("audiobook", ignoreCase = true) ||
                            album.contains("audiobook", ignoreCase = true) ||
                            path.contains("audiobook", ignoreCase = true) ||
                            path.contains("podcasts", ignoreCase = true)

                    val audiobookPos = prefs.getLong("audiobook_pos_$id", 0L)
                    val isFav = prefs.getBoolean("favorite_$id", false)
                    val playCount = prefs.getInt("play_count_$id", 0)
                    val lastPlayed = prefs.getLong("last_played_$id", 0L)

                    // Determine genre heuristically or from path/metadata
                    val genre = when {
                        isAudiobook -> "Audiobook"
                        path.contains("lofi", ignoreCase = true) || title.contains("lofi", ignoreCase = true) -> "Lo-Fi"
                        path.contains("rock", ignoreCase = true) || album.contains("rock", ignoreCase = true) -> "Rock"
                        path.contains("pop", ignoreCase = true) -> "Pop"
                        path.contains("jazz", ignoreCase = true) -> "Jazz"
                        path.contains("classic", ignoreCase = true) -> "Classical"
                        path.contains("chill", ignoreCase = true) -> "Chill"
                        path.contains("electronic", ignoreCase = true) -> "Electronic"
                        else -> "Soundtrack"
                    }

                    tracks.add(
                        MusicTrack(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            durationMs = duration,
                            uri = contentUri,
                            path = path,
                            albumId = albumId,
                            format = ext,
                            sampleRate = sampleRate,
                            bitDepth = bitDepth,
                            bitrateKbps = if (quality == AudioQuality.HI_RES) 1411 else 320,
                            quality = quality,
                            isAudiobook = isAudiobook,
                            audiobookPositionMs = audiobookPos,
                            isFavorite = isFav,
                            playCount = playCount,
                            lastPlayedTimestamp = lastPlayed,
                            year = year,
                            genre = genre,
                            author = author,
                            dateAdded = dateAdded,
                            sizeBytes = sizeBytes
                        )
                    )
                }
            }
        } catch (_: Throwable) {
            // Fail gracefully if permission not granted yet
        }

        tracks
    }

    // ── Hidden Tracks Management ───────────────────────────────────────────────

    fun hideTrack(trackId: Long, hide: Boolean = true) {
        val current = getHiddenTrackIds().toMutableSet()
        if (hide) current.add(trackId) else current.remove(trackId)
        prefs.edit().putString("hidden_track_ids", current.joinToString(",")).apply()
    }

    fun getHiddenTrackIds(): Set<Long> {
        val raw = prefs.getString("hidden_track_ids", "") ?: ""
        if (raw.isBlank()) return emptySet()
        return raw.split(",").mapNotNull { it.trim().toLongOrNull() }.toSet()
    }

    // ── Sorting & Filtering Helpers ───────────────────────────────────────────

    fun sortTracks(
        tracks: List<MusicTrack>,
        option: SortOption,
        ascending: Boolean = true
    ): List<MusicTrack> {
        val sorted = when (option) {
            SortOption.ARTIST -> tracks.sortedBy { it.artist.lowercase() }
            SortOption.AUTHOR -> tracks.sortedBy { it.effectiveAuthor.lowercase() }
            SortOption.ALBUM -> tracks.sortedBy { it.album.lowercase() }
            SortOption.YEAR -> tracks.sortedBy { it.year }
            SortOption.GENRE -> tracks.sortedBy { it.genre.lowercase() }
            SortOption.DATE_ADDED -> tracks.sortedBy { it.dateAdded }
            SortOption.SIZE -> tracks.sortedBy { it.sizeBytes }
            SortOption.DURATION -> tracks.sortedBy { it.durationMs }
            SortOption.CUSTOM -> tracks.sortedByDescending { it.playCount }
        }
        return if (ascending) sorted else sorted.reversed()
    }

    // ── Playback Statistics & History ──────────────────────────────────────────

    fun recordTrackPlayed(trackId: Long) {
        val currentCount = prefs.getInt("play_count_$trackId", 0)
        val now = System.currentTimeMillis()

        val existingRecent = getRecentPlayedIds().toMutableList()
        existingRecent.remove(trackId)
        existingRecent.add(0, trackId)
        val trimmedRecent = existingRecent.take(30)

        prefs.edit()
            .putInt("play_count_$trackId", currentCount + 1)
            .putLong("last_played_$trackId", now)
            .putString("recent_played_ids", trimmedRecent.joinToString(","))
            .apply()
    }

    fun getRecentPlayedIds(): List<Long> {
        val raw = prefs.getString("recent_played_ids", "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(",").mapNotNull { it.trim().toLongOrNull() }
    }

    fun getRecentlyPlayedTracks(allTracks: List<MusicTrack>, limit: Int = 10): List<MusicTrack> {
        val trackMap = allTracks.associateBy { it.id }
        val recentIds = getRecentPlayedIds()
        val recentList = recentIds.mapNotNull { trackMap[it] }.toMutableList()
        if (recentList.size < limit) {
            val remaining = allTracks.filterNot { it.id in recentIds }
                .sortedByDescending { it.lastPlayedTimestamp }
            recentList.addAll(remaining.take(limit - recentList.size))
        }
        return recentList.take(limit)
    }

    fun getMostPlayedTracks(allTracks: List<MusicTrack>, limit: Int = 10): List<MusicTrack> {
        val played = allTracks.filter { it.playCount > 0 }.sortedByDescending { it.playCount }
        if (played.isNotEmpty()) {
            return (played + allTracks.filter { it.playCount == 0 }).take(limit)
        }
        return allTracks.take(limit)
    }

    fun getCuratedMixes(allTracks: List<MusicTrack>): Map<String, List<MusicTrack>> {
        val mixes = mutableMapOf<String, List<MusicTrack>>()
        mixes["Today's Mix"] = allTracks.shuffled().take(15)
        mixes["Deep Focus"] = allTracks.filter { it.durationMs > 180000 }.take(10).ifEmpty { allTracks.take(8) }
        mixes["Lo-Fi Room"] = allTracks.takeLast(10).ifEmpty { allTracks.take(8) }
        mixes["Workout"] = allTracks.shuffled().take(10)
        return mixes
    }

    // ── Search History ────────────────────────────────────────────────────────

    fun getRecentSearches(): List<String> {
        val raw = prefs.getString("recent_searches", "Luna,The Nothing,Chill music,Lo-fi,AuRORA") ?: ""
        return raw.split(";").map { it.trim() }.filter { it.isNotBlank() }.take(8)
    }

    fun addSearchQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        val current = getRecentSearches().toMutableList()
        current.remove(trimmed)
        current.add(0, trimmed)
        prefs.edit().putString("recent_searches", current.take(10).joinToString(";")).apply()
    }

    fun clearSearchHistory() {
        prefs.edit().remove("recent_searches").apply()
    }

    // ── Onboarding ───────────────────────────────────────────────────────────

    fun isOnboardingCompleted(): Boolean = prefs.getBoolean("onboarding_done", false)

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("onboarding_done", completed).apply()
    }

    // ── Favorites & Audiobooks ───────────────────────────────────────────────

    fun setFavorite(trackId: Long, favorite: Boolean) {
        prefs.edit().putBoolean("favorite_$trackId", favorite).apply()
    }

    fun setAudiobook(trackId: Long, isAudiobook: Boolean) {
        prefs.edit().putBoolean("audiobook_$trackId", isAudiobook).apply()
    }

    fun saveAudiobookProgress(trackId: Long, posMs: Long) {
        prefs.edit().putLong("audiobook_pos_$trackId", posMs).apply()
    }

    // ── Settings ─────────────────────────────────────────────────────────────

    fun getAppName(): String = prefs.getString("custom_app_name", "NothingTune") ?: "NothingTune"

    fun setAppName(name: String) {
        prefs.edit().putString("custom_app_name", name).apply()
    }

    fun getThemeIndex(): Int = prefs.getInt("theme_index", 0)

    fun setThemeIndex(idx: Int) {
        prefs.edit().putInt("theme_index", idx).apply()
    }

    fun getAccentColorInt(): Int = prefs.getInt("accent_color", AndroidColor.WHITE)

    fun setAccentColorInt(color: Int) {
        prefs.edit().putInt("accent_color", color).apply()
    }
}
