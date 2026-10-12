package com.nothing.tune

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.nothing.tune.data.MusicRepository
import com.nothing.tune.data.MusicTrack
import com.nothing.tune.player.AudioDeviceManager
import com.nothing.tune.player.AudioPlayerController
import com.nothing.tune.ui.components.MiniPlayerBar
import com.nothing.tune.ui.screens.AlbumDetailScreen
import com.nothing.tune.ui.screens.EqualizerScreen
import com.nothing.tune.ui.screens.HomeScreen
import com.nothing.tune.ui.screens.NowPlayingScreen
import com.nothing.tune.ui.screens.OnboardingScreen
import com.nothing.tune.ui.screens.QueueScreen
import com.nothing.tune.ui.screens.SearchScreen
import com.nothing.tune.ui.screens.SettingsScreen
import com.nothing.tune.ui.screens.SleepTimerScreen
import com.nothing.tune.ui.screens.SplashScreen
import com.nothing.tune.ui.screens.StreamingScreen
import com.nothing.tune.ui.theme.NothingTuneTheme
import com.nothing.tune.ui.theme.PureBlack
import kotlinx.coroutines.launch

sealed interface ScreenNav {
    data object Splash : ScreenNav
    data object Onboarding : ScreenNav
    data object Home : ScreenNav
    data object Search : ScreenNav
    data class AlbumDetail(val title: String, val tracks: List<MusicTrack>) : ScreenNav
    data object NowPlaying : ScreenNav
    data object Queue : ScreenNav
    data object Equalizer : ScreenNav
    data object SleepTimer : ScreenNav
    data object Settings : ScreenNav
    data object Streaming : ScreenNav
}

class MainActivity : ComponentActivity() {

    private lateinit var repository: MusicRepository
    private lateinit var controller: AudioPlayerController
    private lateinit var audioDeviceManager: AudioDeviceManager
    private var onPermissionGrantedCallback: (() -> Unit)? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        onPermissionGrantedCallback?.invoke()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            repository = MusicRepository(this)
            controller = AudioPlayerController(this, repository)
            audioDeviceManager = AudioDeviceManager(this)

            checkAndRequestPermissions()

            setContent {
                var backStack by remember { mutableStateOf<List<ScreenNav>>(listOf(ScreenNav.Splash)) }
                var tracks by remember { mutableStateOf<List<MusicTrack>>(emptyList()) }
                var appName by remember { mutableStateOf(repository.getAppName()) }
                var accentColor by remember { mutableStateOf(Color(repository.getAccentColorInt())) }

                val uiState by controller.uiState.collectAsState()
                val currentScreen = backStack.lastOrNull() ?: ScreenNav.Home

                fun navigateTo(screen: ScreenNav) {
                    backStack = backStack + screen
                }

                fun popBack() {
                    if (backStack.size > 1) {
                        backStack = backStack.dropLast(1)
                    }
                }

                fun replaceWith(screen: ScreenNav) {
                    backStack = listOf(screen)
                }

                BackHandler(enabled = backStack.size > 1) {
                    popBack()
                }

                LaunchedEffect(Unit) {
                    tracks = repository.loadTracks()
                    val lastTrackId = repository.getLastPlaybackTrackId()
                    if (lastTrackId != null && controller.uiState.value.currentTrack == null) {
                        val lastTrack = tracks.firstOrNull { it.id == lastTrackId }
                        if (lastTrack != null) {
                            val lastPos = repository.getLastPlaybackPositionMs()
                            controller.playTrack(lastTrack.copy(audiobookPositionMs = lastPos))
                            controller.player.pause()
                        }
                    }
                    onPermissionGrantedCallback = {
                        lifecycleScope.launch {
                            tracks = repository.loadTracks()
                        }
                    }
                }

                NothingTuneTheme(accentColor = accentColor) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PureBlack)
                    ) {
                        // ── Screen Content ─────────────────────────────────────
                        when (val scr = currentScreen) {
                            is ScreenNav.Splash -> {
                                SplashScreen(
                                    onFinish = {
                                        if (repository.isOnboardingCompleted()) {
                                            replaceWith(ScreenNav.Home)
                                        } else {
                                            replaceWith(ScreenNav.Onboarding)
                                        }
                                    }
                                )
                            }

                            is ScreenNav.Onboarding -> {
                                OnboardingScreen(
                                    onGetStarted = {
                                        repository.setOnboardingCompleted(true)
                                        replaceWith(ScreenNav.Home)
                                    }
                                )
                            }

                            is ScreenNav.Home -> {
                                HomeScreen(
                                    tracks = tracks,
                                    controller = controller,
                                    repository = repository,
                                    onOpenNowPlaying = { navigateTo(ScreenNav.NowPlaying) },
                                    onOpenSearch = { navigateTo(ScreenNav.Search) },
                                    onOpenSettings = { navigateTo(ScreenNav.Settings) },
                                    onOpenStreaming = { navigateTo(ScreenNav.Streaming) },
                                    onOpenPlaylistDetail = { title, plTracks ->
                                        navigateTo(ScreenNav.AlbumDetail(title, plTracks))
                                    },
                                    onToggleFavorite = { trackId, isFav ->
                                        repository.setFavorite(trackId, isFav)
                                        tracks = tracks.map { if (it.id == trackId) it.copy(isFavorite = isFav) else it }
                                    },
                                    onToggleAudiobook = { trackId, isBook ->
                                        repository.setAudiobook(trackId, isBook)
                                        tracks = tracks.map { if (it.id == trackId) it.copy(isAudiobook = isBook) else it }
                                    },
                                    onHideTrack = { trackId ->
                                        repository.hideTrack(trackId, true)
                                        tracks = tracks.filterNot { it.id == trackId }
                                    },
                                    onHideMultipleTracks = { ids ->
                                        repository.hideMultipleTracks(ids)
                                        tracks = tracks.filterNot { ids.contains(it.id) }
                                    }
                                )
                            }

                            is ScreenNav.Search -> {
                                SearchScreen(
                                    tracks = tracks,
                                    controller = controller,
                                    repository = repository,
                                    onBack = { popBack() },
                                    onOpenNowPlaying = { navigateTo(ScreenNav.NowPlaying) }
                                )
                            }

                            is ScreenNav.AlbumDetail -> {
                                AlbumDetailScreen(
                                    title = scr.title,
                                    tracks = scr.tracks,
                                    controller = controller,
                                    onBack = { popBack() },
                                    onOpenNowPlaying = { navigateTo(ScreenNav.NowPlaying) },
                                    onToggleFavorite = { trackId, isFav ->
                                        repository.setFavorite(trackId, isFav)
                                        tracks = tracks.map { if (it.id == trackId) it.copy(isFavorite = isFav) else it }
                                    }
                                )
                            }

                            is ScreenNav.NowPlaying -> {
                                NowPlayingScreen(
                                    controller = controller,
                                    audioDeviceManager = audioDeviceManager,
                                    accentColor = accentColor,
                                    onClose = { popBack() },
                                    onOpenEqualizer = { navigateTo(ScreenNav.Equalizer) },
                                    onOpenSleepTimer = { navigateTo(ScreenNav.SleepTimer) },
                                    onOpenQueue = { navigateTo(ScreenNav.Queue) },
                                    onToggleFavorite = { trackId, isFav ->
                                        repository.setFavorite(trackId, isFav)
                                        tracks = tracks.map { if (it.id == trackId) it.copy(isFavorite = isFav) else it }
                                    }
                                )
                            }

                            is ScreenNav.Queue -> {
                                QueueScreen(
                                    controller = controller,
                                    onBack = { popBack() }
                                )
                            }

                            is ScreenNav.Equalizer -> {
                                EqualizerScreen(
                                    controller = controller,
                                    onBack = { popBack() }
                                )
                            }

                            is ScreenNav.SleepTimer -> {
                                SleepTimerScreen(
                                    controller = controller,
                                    onBack = { popBack() }
                                )
                            }

                            is ScreenNav.Settings -> {
                                SettingsScreen(
                                    repository = repository,
                                    audioDeviceManager = audioDeviceManager,
                                    currentAppName = appName,
                                    accentColor = accentColor,
                                    onAppNameChange = { newName ->
                                        appName = newName
                                        repository.setAppName(newName)
                                    },
                                    onAccentColorChange = { newColor ->
                                        accentColor = newColor
                                        repository.setAccentColorInt(newColor.toArgb())
                                    },
                                    onBack = { popBack() }
                                )
                            }

                            is ScreenNav.Streaming -> {
                                StreamingScreen(
                                    controller = controller,
                                    accentColor = accentColor,
                                    onBack = { popBack() },
                                    onTrackStarted = { navigateTo(ScreenNav.NowPlaying) }
                                )
                            }
                        }

                        // ── Persistent Mini Player Bar Overlay ─────────────────
                        val showMiniPlayer = uiState.currentTrack != null &&
                                currentScreen !is ScreenNav.Splash &&
                                currentScreen !is ScreenNav.Onboarding &&
                                currentScreen !is ScreenNav.NowPlaying

                        if (showMiniPlayer) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                            ) {
                                MiniPlayerBar(
                                    track = uiState.currentTrack,
                                    isPlaying = uiState.isPlaying,
                                    onTogglePlayPause = { controller.togglePlayPause() },
                                    onClick = { navigateTo(ScreenNav.NowPlaying) },
                                    onOpenQueue = { navigateTo(ScreenNav.Queue) }
                                )
                            }
                        }
                    }
                }
            }
        } catch (t: Throwable) {
            Log.e("NothingTune", "Startup error", t)
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }

        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::controller.isInitialized) {
            controller.release()
        }
        if (::audioDeviceManager.isInitialized) {
            audioDeviceManager.release()
        }
    }
}
