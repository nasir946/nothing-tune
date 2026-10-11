// ─────────────────────────────────────────────────────────────────────────────
// AppConfig — change the app name and other global constants here.
// ─────────────────────────────────────────────────────────────────────────────

class AppConfig {
  AppConfig._();

  /// The display name of the app shown in the UI and system dialogs.
  static const String appName = 'NothingTune';

  /// Android package identifier — must match android/app/build.gradle
  static const String packageName = 'com.nothing.tune';

  /// Minimum supported Android API level
  static const int minAndroidApi = 26;

  /// App version string
  static const String version = '1.0.0';

  // ── Last.fm ────────────────────────────────────────────────────────────────
  /// Register a free API key at https://www.last.fm/api/account/create
  /// and paste it here, or let the user enter it in Settings.
  static const String lastFmApiKey = '';
  static const String lastFmSharedSecret = '';
  static const String lastFmCallbackScheme = 'nothingtune';

  // ── MusicBrainz ────────────────────────────────────────────────────────────
  /// MusicBrainz is free — no key needed. This is the user-agent string.
  static const String musicBrainzUserAgent =
      '$appName/$version (${packageName})';

  // ── Scrobble threshold ─────────────────────────────────────────────────────
  /// Scrobble when playback reaches this fraction (0.5 = 50 %) OR 4 minutes.
  static const double scrobbleThreshold = 0.50;
  static const Duration scrobbleMaxDuration = Duration(minutes: 4);
}
