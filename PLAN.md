# NothingTune — Music Player · Full Build Plan

> Nothing OS-themed Flutter music player for Android (API 26+)
> App name is configurable via `lib/config/app_config.dart`

---

## Stack

| Layer | Technology |
|---|---|
| Framework | Flutter (Dart) |
| Audio engine | just_audio + just_audio_background |
| Background session | audio_service |
| Database | Isar (NoSQL, native Dart) |
| State management | Riverpod |
| HTTP / metadata | dio + MusicBrainz REST API |
| Streaming / Download | yt-dlp binary (bundled) + youtube_explode_dart fallback |
| Scrobbling | Last.fm API (OAuth) |
| Fonts | Space Mono (Google Fonts) |
| Platform channels (Kotlin) | MediaStore scanner · Equalizer API · AudioTrack Hi-Res · yt-dlp process |

---

## Feature List

### Audio Playback
- [x] Hi-Res audio passthrough (24-bit / 192 kHz) via Android AudioTrack exclusive mode
- [x] Supported formats: FLAC · WAV · AIFF · ALAC · MP3 · AAC · OGG · OPUS · WMA · M4A · APE · TTA · MPC · DSD (via conversion)
- [x] Background playback with system notification controls
- [x] Queue management (add, reorder, remove)
- [x] Shuffle · Repeat (off / one / all)
- [x] Gapless playback
- [x] Sleep timer
- [x] Speed / pitch control

### Library & Filters
- [x] Songs · Albums · Artists · Genres screens
- [x] Quality filter: Hi-Res (≥ 24-bit) · Lossless (FLAC/ALAC/WAV) · Standard (MP3/AAC)
- [x] Audiobook mode — auto-resume position, chapter support (if tagged)
- [x] Smart playlists: Recently Added · Most Played · Unplayed · Favorites
- [x] User playlists (create, rename, delete, reorder)
- [x] Global search (song / album / artist / genre)
- [x] Sort: A–Z · Z–A · Date Added · Duration · Play Count

### Now Playing Screen
- [x] Full-screen album art with dot-matrix overlay painter
- [x] Nothing OS animated bars (CustomPainter + AnimationController) while playing
- [x] Rotatable round volume knob (drag gesture)
- [x] Knob tap → inline 5-band EQ strip slides up
- [x] Seek bar with elapsed / remaining time
- [x] Swipe left/right to skip tracks
- [x] Lyrics display (embedded LRC or fetched online)

### Equalizer
- [x] 10-band equalizer (Android Equalizer API via platform channel)
- [x] Presets: Flat · Bass Boost · Rock · Pop · Classical · Hip-Hop · Jazz · Custom
- [x] BassBoost slider
- [x] Virtualizer (surround) toggle
- [x] Loudness Enhancer toggle
- [x] Inline EQ strip (5 bands) accessible from volume knob on Now Playing
- [x] Full EQ screen accessible from settings / player menu

### Streaming & Download
- [x] Paste any URL (YouTube · SoundCloud · direct MP3/FLAC/m3u link)
- [x] yt-dlp resolves stream info (bundled binary, no server needed)
- [x] Quality picker before download: Best · 320k MP3 · 128k MP3 · Original (FLAC if available)
- [x] Download queue with per-item progress bar
- [x] Internet Radio: add station URL (m3u / HLS), station list, stream via just_audio
- [x] Background download (WorkManager via platform channel)

### Online Services
- [x] MusicBrainz API — auto-fetch missing album art & metadata
- [x] Last.fm OAuth login
- [x] Last.fm scrobbling (after 50% playback or 4 min, whichever first)
- [x] Last.fm "Now Playing" update
- [x] Metadata editor — edit tags locally, write back to file

### Theme System (runtime-switchable, no restart)
| Theme | Description |
|---|---|
| Dot Mono | Pure black #000000 background, white text/icons, dot-matrix overlay, Space Mono font |
| Dot + Accent | Same as Mono but with a user-chosen accent color (color picker) |
| Glassmorphism | Dark blur cards, frosted glass panels, subtle gradients |

### Responsive / Freeform Layout
| Window width | Layout |
|---|---|
| < 300 dp | Compact: album art + play/pause only |
| 300–600 dp | Standard portrait player (phone) |
| > 600 dp | Two-pane: library list + now-playing side by side (tablet / freeform) |

- Mini player persistent bottom bar (collapses in compact mode)
- Freeform multi-window resize supported (Android freeform mode)

---

## Folder Structure

```
nothing_tune/
├── android/
│   └── app/src/main/kotlin/com/nothing/tune/
│       ├── MainActivity.kt
│       ├── MediaStorePlugin.kt        ← scans device library via MediaStore
│       ├── YtDlpPlugin.kt             ← invokes bundled yt-dlp binary
│       ├── EqualizerPlugin.kt         ← Android Equalizer / BassBoost / Virtualizer
│       └── HiResAudioPlugin.kt        ← AudioTrack exclusive Hi-Res mode
├── assets/
│   ├── fonts/SpaceMono/               ← Space Mono TTF files
│   ├── bin/
│   │   ├── yt-dlp-arm64               ← yt-dlp binary (arm64-v8a)
│   │   └── yt-dlp-arm32               ← yt-dlp binary (armeabi-v7a)
│   └── images/
│       └── placeholder_art.png
├── lib/
│   ├── main.dart
│   ├── config/
│   │   └── app_config.dart            ← APP NAME · theme seed · constants
│   ├── app/
│   │   ├── router.dart                ← go_router route definitions
│   │   └── theme/
│   │       ├── app_theme.dart         ← ThemeData builders for 3 themes
│   │       ├── dot_matrix_painter.dart← CustomPainter dot-matrix overlay
│   │       └── glassmorphism.dart     ← blur card widget
│   ├── providers/                     ← Riverpod providers (global)
│   │   ├── audio_provider.dart
│   │   ├── library_provider.dart
│   │   ├── theme_provider.dart
│   │   ├── eq_provider.dart
│   │   └── download_provider.dart
│   ├── models/
│   │   ├── song.dart / song.g.dart    ← Isar collection
│   │   ├── album.dart / album.g.dart
│   │   ├── artist.dart / artist.g.dart
│   │   ├── genre.dart / genre.g.dart
│   │   ├── playlist.dart / playlist.g.dart
│   │   ├── radio_station.dart / radio_station.g.dart
│   │   └── download_task.dart / download_task.g.dart
│   ├── data/
│   │   ├── database/
│   │   │   └── isar_service.dart      ← singleton Isar instance + CRUD
│   │   ├── scanner/
│   │   │   └── media_scanner.dart     ← calls MediaStorePlugin, populates DB
│   │   ├── streaming/
│   │   │   ├── ytdlp_service.dart     ← calls YtDlpPlugin
│   │   │   └── radio_service.dart     ← m3u parser, HLS stream setup
│   │   ├── metadata/
│   │   │   ├── musicbrainz_client.dart← REST calls, art download
│   │   │   └── tag_writer.dart        ← writes ID3/Vorbis tags back to file
│   │   └── lastfm/
│   │       ├── lastfm_auth.dart       ← OAuth flow
│   │       └── lastfm_scrobbler.dart  ← scrobble + now-playing calls
│   ├── audio/
│   │   ├── audio_handler.dart         ← BaseAudioHandler implementation
│   │   ├── audio_player_service.dart  ← queue, shuffle, repeat logic
│   │   └── hires_channel.dart         ← MethodChannel for HiResAudioPlugin
│   ├── equalizer/
│   │   ├── eq_service.dart            ← MethodChannel for EqualizerPlugin
│   │   └── eq_presets.dart            ← preset band values
│   ├── ui/
│   │   ├── library/
│   │   │   ├── library_screen.dart    ← tab host (Songs/Albums/Artists/Genre/Playlists)
│   │   │   ├── songs_tab.dart
│   │   │   ├── albums_tab.dart
│   │   │   ├── artists_tab.dart
│   │   │   ├── genres_tab.dart
│   │   │   ├── playlists_tab.dart
│   │   │   ├── audiobooks_tab.dart
│   │   │   └── filter_bar.dart        ← quality / sort filter chips
│   │   ├── player/
│   │   │   ├── now_playing_screen.dart
│   │   │   └── mini_player.dart
│   │   ├── equalizer/
│   │   │   ├── eq_screen.dart         ← full 10-band EQ
│   │   │   └── inline_eq_strip.dart   ← 5-band strip shown above knob
│   │   ├── streaming/
│   │   │   ├── url_paste_screen.dart
│   │   │   ├── download_queue_screen.dart
│   │   │   └── radio_screen.dart
│   │   ├── settings/
│   │   │   └── settings_screen.dart
│   │   └── widgets/
│   │       ├── waveform_bars.dart     ← animated Nothing-style bars
│   │       ├── volume_knob.dart       ← rotatable round knob widget
│   │       ├── dot_overlay.dart       ← dot-matrix art overlay widget
│   │       ├── album_art.dart         ← art with fallback + cache
│   │       └── song_tile.dart         ← reusable list tile
│   └── utils/
│       ├── format_utils.dart          ← duration · file size · codec detection
│       └── color_utils.dart           ← accent extraction from album art
├── pubspec.yaml
└── PLAN.md                            ← this file
```

---

## Build Phases

### Phase 1 — Foundation ✅ (current)
- Flutter project scaffold
- `AppConfig` with configurable app name
- `pubspec.yaml` with all dependencies
- Isar models (Song, Album, Artist, Genre, Playlist, RadioStation, DownloadTask)
- Permission handling (READ_MEDIA_AUDIO / READ_EXTERNAL_STORAGE)
- `MediaStorePlugin.kt` — scan device library
- `MediaScanner` dart service — populate Isar from scan results
- `just_audio` + `audio_service` — background playback
- `AudioHandler` + `AudioPlayerService`
- Basic Library screen (Songs list, play on tap)

### Phase 2 — Library & Filters
- Albums, Artists, Genres, Audiobooks, Playlists screens
- Quality detection + filter chips
- Smart playlists
- Search
- Sort options

### Phase 3 — Now Playing Screen
- Full-screen player UI
- Dot-matrix art overlay (`CustomPainter`)
- Animated waveform bars
- Rotatable volume knob
- Inline EQ strip (tap knob)
- Seek bar, shuffle, repeat, queue

### Phase 4 — Full Equalizer
- 10-band EQ platform channel
- Presets, BassBoost, Virtualizer, Loudness Enhancer
- Full EQ screen UI

### Phase 5 — Streaming & Download
- yt-dlp platform channel + bundled binary
- URL paste dialog
- Quality picker + download queue
- Internet Radio (m3u/HLS)

### Phase 6 — Online Services
- MusicBrainz metadata fetch
- Last.fm OAuth + scrobbling
- Metadata/tag editor

### Phase 7 — Theme System & Responsive UI
- 3 runtime themes (Dot Mono / Dot+Accent / Glass)
- Accent color picker
- Responsive LayoutBuilder (compact / standard / two-pane)
- Mini player
- Nothing OS dot ambient lock screen art

### Phase 8 — Final Polish
- Onboarding flow (first-run)
- App icon
- Android 13+ permissions
- Performance (lazy load, image cache, DB index)
- Sleep timer, speed/pitch controls
- Lyrics (LRC embedded / online fetch)

---

## Key Constants

```dart
// lib/config/app_config.dart
class AppConfig {
  static const String appName = 'NothingTune'; // ← change app name here
  static const String packageName = 'com.nothing.tune';
  static const int minAndroidApi = 26;
  static const String version = '1.0.0';
}
```

---

## APK Size Estimate
~35–45 MB (Flutter engine ~10 MB + yt-dlp arm64 binary ~8 MB + assets)

---

## Notes
- yt-dlp binary is bundled in `assets/bin/`. On first run it is copied to the app's
  files directory and made executable. Updated by re-installing the APK.
- Space Mono can be swapped for NDot or any monospace font by replacing files in
  `assets/fonts/SpaceMono/` and updating `pubspec.yaml`.
- SoundCloud is handled by yt-dlp (no API key required).
- Last.fm requires a free API key — entered by the user in Settings on first use.
- MusicBrainz is free / no key required.
