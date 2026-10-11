package com.nothing.tune.player

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.nothing.tune.MainActivity
import com.nothing.tune.data.MusicRepository
import com.nothing.tune.data.MusicTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlayerUiState(
    val currentTrack: MusicTrack? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val volume: Float = 0.8f,
    val playbackSpeed: Float = 1.0f,
    val queue: List<MusicTrack> = emptyList(),
    val queueIndex: Int = 0,
    // Equalizer state
    val eqEnabled: Boolean = true,
    val eqBands: List<Short> = List(7) { 0.toShort() },
    val eqFreqs: List<Int> = listOf(60, 150, 400, 1000, 2500, 6000, 14000),
    val minBandLevel: Short = -1500,
    val maxBandLevel: Short = 1500,
    val bassBoost: Int = 0,
    val virtualizer: Int = 0,
    val currentPreset: String = "Custom",
    // Sleep Timer state
    val sleepTimerRemainingSeconds: Int? = null,
    val sleepTimerMinutes: Int? = null,
    val stopAfterCurrentSong: Boolean = false
)

class AudioPlayerController(
    private val context: Context,
    private val repository: MusicRepository
) {
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .build()

    val player: ExoPlayer = ExoPlayer.Builder(context.applicationContext)
        .setAudioAttributes(audioAttributes, true)
        .setHandleAudioBecomingNoisy(true)
        .setWakeMode(C.WAKE_MODE_LOCAL)
        .build()

    val mediaSession: MediaSession

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val prefs: SharedPreferences =
        context.getSharedPreferences("nothing_tune_eq_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    val visualizer = RealtimeAudioVisualizer(context)

    private var equalizer: Equalizer? = null
    private var bassBoostEffect: BassBoost? = null
    private var virtualizerEffect: Virtualizer? = null
    private var audioEffectsInitialized = false

    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null

    init {
        activeInstance = this

        val sessionActivityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            context,
            0,
            sessionActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(context.applicationContext, player)
            .setSessionActivity(sessionActivityPendingIntent)
            .build()

        loadSavedEqSettings()
        setupPlayerListeners()
        startProgressPolling()
    }

    private fun loadSavedEqSettings() {
        val enabled = prefs.getBoolean("eq_enabled", true)
        val preset = prefs.getString("eq_preset", "Custom") ?: "Custom"
        val bass = prefs.getInt("eq_bass", 0)
        val virt = prefs.getInt("eq_virt", 0)

        val savedBands = (0 until 7).map {
            prefs.getInt("eq_band_$it", 0).toShort()
        }

        _uiState.value = _uiState.value.copy(
            eqEnabled = enabled,
            currentPreset = preset,
            bassBoost = bass,
            virtualizer = virt,
            eqBands = savedBands
        )
    }

    private fun setupPlayerListeners() {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
                if (!isPlaying) {
                    savePlaybackState()
                } else {
                    ensureAudioEffects()
                    _uiState.value.currentTrack?.let { track ->
                        repository.recordTrackPlayed(track.id)
                        repository.saveLastPlaybackState(track.id, player.currentPosition)
                    }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _uiState.value = _uiState.value.copy(
                        durationMs = player.duration.coerceAtLeast(0L)
                    )
                    ensureAudioEffects()
                } else if (playbackState == Player.STATE_ENDED) {
                    if (_uiState.value.stopAfterCurrentSong) {
                        player.pause()
                        cancelSleepTimer()
                    } else {
                        skipNext()
                    }
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                _uiState.value = _uiState.value.copy(
                    currentPositionMs = player.currentPosition.coerceAtLeast(0L)
                )
            }
        })
    }

    private fun ensureAudioEffects() {
        val sessionId = player.audioSessionId
        if (sessionId <= 0) return

        try {
            visualizer.attachSession(sessionId)

            if (equalizer == null || audioEffectsInitialized.not()) {
                equalizer = Equalizer(1000, sessionId)
                bassBoostEffect = BassBoost(1000, sessionId)
                virtualizerEffect = Virtualizer(1000, sessionId)

                equalizer?.let { eq ->
                    eq.enabled = _uiState.value.eqEnabled
                    val range = eq.bandLevelRange
                    val minL = range[0]
                    val maxL = range[1]

                    val numBands = eq.numberOfBands.toInt()
                    val actualFreqs = (0 until numBands).map { (eq.getCenterFreq(it.toShort()) / 1000).toInt() }

                    _uiState.value = _uiState.value.copy(
                        minBandLevel = minL,
                        maxBandLevel = maxL,
                        eqFreqs = if (actualFreqs.isNotEmpty()) actualFreqs else _uiState.value.eqFreqs
                    )

                    reapplyEqToHardware()
                }

                bassBoostEffect?.let { bb ->
                    bb.enabled = true
                    bb.setStrength(_uiState.value.bassBoost.coerceIn(0, 1000).toShort())
                }

                virtualizerEffect?.let { virt ->
                    virt.enabled = true
                    virt.setStrength(_uiState.value.virtualizer.coerceIn(0, 1000).toShort())
                }

                audioEffectsInitialized = true
            } else {
                reapplyEqToHardware()
            }
        } catch (_: Throwable) {
            // AudioFX unsupported on some hardware
        }
    }

    private fun reapplyEqToHardware() {
        val eq = equalizer ?: return
        val numBands = eq.numberOfBands.toInt()
        val levels = _uiState.value.eqBands

        for (i in 0 until numBands) {
            val level = levels.getOrElse(i) { 0.toShort() }
            val clamped = level.coerceIn(_uiState.value.minBandLevel, _uiState.value.maxBandLevel)
            try {
                eq.setBandLevel(i.toShort(), clamped)
            } catch (_: Throwable) {}
        }
    }

    private fun startProgressPolling() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                if (player.isPlaying) {
                    val pos = player.currentPosition.coerceAtLeast(0L)
                    _uiState.value = _uiState.value.copy(currentPositionMs = pos)
                    visualizer.updateSimulatedFrame(true, pos)

                    _uiState.value.currentTrack?.let { track ->
                        if (track.isAudiobook) {
                            repository.saveAudiobookProgress(track.id, pos)
                        }
                    }
                } else {
                    visualizer.updateSimulatedFrame(false, 0L)
                }
                delay(120)
            }
        }
    }

    fun playTrack(track: MusicTrack, queue: List<MusicTrack>? = null, index: Int = 0) {
        savePlaybackState()

        val activeQueue = queue ?: if (_uiState.value.queue.isNotEmpty()) _uiState.value.queue else listOf(track)
        val activeIndex = if (queue != null) index else {
            val idx = activeQueue.indexOfFirst { it.id == track.id }
            if (idx >= 0) idx else 0
        }

        _uiState.value = _uiState.value.copy(
            currentTrack = track,
            queue = activeQueue,
            queueIndex = activeIndex
        )

        // Build MediaItem with rich MediaMetadata for Samsung One UI 9 & Android SystemUI
        val metadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.album)
            .setArtworkUri(track.artworkUri)
            .setDisplayTitle(track.title)
            .setIsPlayable(true)
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaId(track.id.toString())
            .setUri(track.uri)
            .setMediaMetadata(metadata)
            .build()

        player.setMediaItem(mediaItem)

        val startPos = if (track.isAudiobook && track.audiobookPositionMs > 0) {
            track.audiobookPositionMs
        } else {
            0L
        }

        player.seekTo(startPos)
        player.prepare()
        player.play()
        repository.recordTrackPlayed(track.id)

        // Ensure MediaSessionService is running so SystemUI connects
        try {
            val serviceIntent = Intent(context, PlaybackService::class.java)
            context.startService(serviceIntent)
        } catch (_: Throwable) {}
    }

    fun playStreamUrl(url: String, title: String) {
        val streamTrack = MusicTrack(
            id = System.currentTimeMillis(),
            title = title,
            artist = "Live Stream",
            album = "Nothing Internet Radio",
            durationMs = 0L,
            uri = android.net.Uri.parse(url),
            path = url
        )
        playTrack(streamTrack)
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        _uiState.value = _uiState.value.copy(currentPositionMs = positionMs)
    }

    fun skipNext() {
        val state = _uiState.value
        if (state.queue.isEmpty()) return
        val nextIdx = if (state.isShuffle) {
            (state.queue.indices).random()
        } else {
            (state.queueIndex + 1) % state.queue.size
        }
        playTrack(state.queue[nextIdx], state.queue, nextIdx)
    }

    fun skipPrevious() {
        val state = _uiState.value
        if (state.queue.isEmpty()) return
        if (player.currentPosition > 3000) {
            seekTo(0)
        } else {
            val prevIdx = if (state.queueIndex - 1 < 0) state.queue.size - 1 else state.queueIndex - 1
            playTrack(state.queue[prevIdx], state.queue, prevIdx)
        }
    }

    fun toggleShuffle() {
        val newShuffle = !_uiState.value.isShuffle
        _uiState.value = _uiState.value.copy(isShuffle = newShuffle)
    }

    fun cycleRepeatMode() {
        val nextMode = when (_uiState.value.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        player.repeatMode = nextMode
        _uiState.value = _uiState.value.copy(repeatMode = nextMode)
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        player.volume = clamped
        _uiState.value = _uiState.value.copy(volume = clamped)
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.25f, 3.0f)
        player.setPlaybackSpeed(clamped)
        _uiState.value = _uiState.value.copy(playbackSpeed = clamped)
    }

    // ── Queue Management ──────────────────────────────────────────────────────

    fun addToQueue(track: MusicTrack) {
        val updated = _uiState.value.queue.toMutableList()
        updated.add(track)
        _uiState.value = _uiState.value.copy(queue = updated)
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        val current = _uiState.value.queue.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            val currentTrack = _uiState.value.currentTrack
            val newIdx = current.indexOfFirst { it.id == currentTrack?.id }
            _uiState.value = _uiState.value.copy(
                queue = current,
                queueIndex = if (newIdx >= 0) newIdx else _uiState.value.queueIndex
            )
        }
    }

    fun removeFromQueue(index: Int) {
        val current = _uiState.value.queue.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            val currentTrack = _uiState.value.currentTrack
            val newIdx = current.indexOfFirst { it.id == currentTrack?.id }
            _uiState.value = _uiState.value.copy(
                queue = current,
                queueIndex = if (newIdx >= 0) newIdx else 0
            )
        }
    }

    fun clearQueue() {
        val current = _uiState.value.currentTrack
        _uiState.value = _uiState.value.copy(
            queue = if (current != null) listOf(current) else emptyList(),
            queueIndex = 0
        )
    }

    fun playQueueIndex(index: Int) {
        val q = _uiState.value.queue
        if (index in q.indices) {
            playTrack(q[index], q, index)
        }
    }

    // ── Sleep Timer ───────────────────────────────────────────────────────────

    fun startSleepTimer(minutes: Int, stopAfterCurrentSong: Boolean = false) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            cancelSleepTimer()
            return
        }

        val totalSeconds = minutes * 60
        _uiState.value = _uiState.value.copy(
            sleepTimerMinutes = minutes,
            sleepTimerRemainingSeconds = totalSeconds,
            stopAfterCurrentSong = stopAfterCurrentSong
        )

        sleepTimerJob = scope.launch {
            var rem = totalSeconds
            while (rem > 0) {
                delay(1000)
                rem--
                _uiState.value = _uiState.value.copy(sleepTimerRemainingSeconds = rem)
            }
            player.pause()
            cancelSleepTimer()
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _uiState.value = _uiState.value.copy(
            sleepTimerMinutes = null,
            sleepTimerRemainingSeconds = null,
            stopAfterCurrentSong = false
        )
    }

    fun setStopAfterCurrentSong(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(stopAfterCurrentSong = enabled)
    }

    // ── Equalizer APIs ────────────────────────────────────────────────────────

    fun toggleEq(enabled: Boolean) {
        try {
            ensureAudioEffects()
            equalizer?.enabled = enabled
            prefs.edit().putBoolean("eq_enabled", enabled).apply()
            _uiState.value = _uiState.value.copy(eqEnabled = enabled)
        } catch (_: Throwable) {}
    }

    fun setEqBandLevel(band: Int, milliDb: Short) {
        try {
            ensureAudioEffects()
            val eq = equalizer
            if (eq != null) {
                val numBands = eq.numberOfBands.toInt()
                if (band in 0 until numBands) {
                    val clamped = milliDb.coerceIn(_uiState.value.minBandLevel, _uiState.value.maxBandLevel)
                    eq.setBandLevel(band.toShort(), clamped)
                }
            }
            val updated = _uiState.value.eqBands.toMutableList()
            if (band in updated.indices) {
                updated[band] = milliDb
                prefs.edit().putInt("eq_band_$band", milliDb.toInt()).apply()
            }
            prefs.edit().putString("eq_preset", "Custom").apply()
            _uiState.value = _uiState.value.copy(eqBands = updated, currentPreset = "Custom")
        } catch (_: Throwable) {}
    }

    fun setBassBoost(strength: Int) {
        try {
            ensureAudioEffects()
            val clamped = strength.coerceIn(0, 1000)
            bassBoostEffect?.setStrength(clamped.toShort())
            prefs.edit().putInt("eq_bass", clamped).apply()
            _uiState.value = _uiState.value.copy(bassBoost = clamped)
        } catch (_: Throwable) {}
    }

    fun setVirtualizer(strength: Int) {
        try {
            ensureAudioEffects()
            val clamped = strength.coerceIn(0, 1000)
            virtualizerEffect?.setStrength(clamped.toShort())
            prefs.edit().putInt("eq_virt", clamped).apply()
            _uiState.value = _uiState.value.copy(virtualizer = clamped)
        } catch (_: Throwable) {}
    }

    fun applyPreset(presetName: String) {
        val presets = mapOf(
            "Custom" to listOf(0, 0, 0, 0, 0, 0, 0),
            "Bass Boost" to listOf(900, 700, 300, 0, 0, 0, 0),
            "Vocal" to listOf(-200, 100, 500, 700, 500, 200, 0),
            "Balanced" to listOf(300, 200, 0, 0, 100, 200, 300),
            "Treble Boost" to listOf(-200, -100, 0, 200, 500, 800, 1000),
            "Lo-Fi" to listOf(600, 500, 200, -200, -400, -600, -900)
        )
        val levels = presets[presetName] ?: return
        levels.forEachIndexed { index, level ->
            setEqBandLevel(index, level.toShort())
        }
        prefs.edit().putString("eq_preset", presetName).apply()
        _uiState.value = _uiState.value.copy(currentPreset = presetName)
    }

    private fun savePlaybackState() {
        val track = _uiState.value.currentTrack ?: return
        repository.saveLastPlaybackState(track.id, player.currentPosition)
        if (track.isAudiobook) {
            repository.saveAudiobookProgress(track.id, player.currentPosition)
        }
    }

    fun release() {
        savePlaybackState()
        progressJob?.cancel()
        sleepTimerJob?.cancel()
        visualizer.release()
        try {
            mediaSession.release()
            equalizer?.release()
            bassBoostEffect?.release()
            virtualizerEffect?.release()
            player.release()
        } catch (_: Throwable) {}
        activeInstance = null
    }

    companion object {
        var activeInstance: AudioPlayerController? = null
    }
}
