package com.nothing.tune.data

data class ChangeRelease(
    val version: String,
    val date: String,
    val changes: List<String>
)

object AppVersionInfo {
    const val CURRENT_VERSION_NAME = "1.0.11"
    const val CURRENT_VERSION_CODE = 11
    const val RELEASE_DATE = "October 10, 2026"
    const val DEVELOPER_NAME = "Nasir"
    const val ARCHITECTURE = "Kotlin 2.0 · Jetpack Compose · Media3 ExoPlayer"
    const val UI_SYSTEM = "Nothing OS DotMatrix & Glyph Design"
    const val AUDIO_ENGINE = "24-bit / 192kHz Passthrough Direct DAC & AudioFX"

    val CHANGELOG: List<ChangeRelease> = listOf(
        ChangeRelease(
            version = "1.0.11",
            date = "Oct 10, 2026",
            changes = listOf(
                "Notification Panel: Active playback controls in Android notification drawer with MediaStyle (Play/Pause, Next, Previous).",
                "Real-Time FFT Visualizer: Replaced static loop with live audio frequency spectrum analysis (low pitch/bass, mid vocals, high treble).",
                "Equalizer Engine: Fixed customization with dynamic hardware band discovery, continuous dragging, and instant DSP persistence.",
                "System Update & Developer Info: Added version tracking, release notes, and developer diagnostics inside Settings."
            )
        ),
        ChangeRelease(
            version = "1.0.10",
            date = "Oct 10, 2026",
            changes = listOf(
                "Advanced Sorting & Filtering: Filter by Artist, Author, Album, Year, Genre, Date, Size, Duration, and Custom.",
                "Connected Devices: Hardware device detection and Android Media Output multi-device/Dual Audio routing.",
                "Audiobooks Hub: Added bookmarking, progress saving, and 'Add as Book' menu options.",
                "Online Streaming & Downloader: Paste YouTube, SoundCloud, or direct audio links to stream or download to device.",
                "List Management: Added hide/remove track from library and playlist options."
            )
        ),
        ChangeRelease(
            version = "1.0.0",
            date = "Oct 10, 2026",
            changes = listOf(
                "Initial Nothing OS release with 10-screen layout, dot-matrix typography, and Media3 audio playback."
            )
        )
    )
}
