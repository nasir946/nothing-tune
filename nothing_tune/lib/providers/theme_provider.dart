import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../models/song.dart';

// ── Theme ──────────────────────────────────────────────────────────────────

enum AppThemeMode { dotMono, dotAccent, glass }

class ThemeNotifier extends Notifier<AppThemeMode> {
  static const _key = 'themeMode';

  @override
  AppThemeMode build() {
    _loadAsync();
    return AppThemeMode.dotMono;
  }

  Future<void> _loadAsync() async {
    final prefs = await SharedPreferences.getInstance();
    final idx = prefs.getInt(_key) ?? 0;
    state = AppThemeMode.values[idx.clamp(0, AppThemeMode.values.length - 1)];
  }

  Future<void> setTheme(AppThemeMode mode) async {
    state = mode;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setInt(_key, mode.index);
  }
}

final themeProvider = NotifierProvider<ThemeNotifier, AppThemeMode>(
  ThemeNotifier.new,
);

// ── Accent color ────────────────────────────────────────────────────────────

class AccentColorNotifier extends Notifier<int> {
  static const _key = 'accentColor';
  static const _defaultColor = 0xFFFFFFFF; // white

  @override
  int build() {
    _loadAsync();
    return _defaultColor;
  }

  Future<void> _loadAsync() async {
    final prefs = await SharedPreferences.getInstance();
    state = prefs.getInt(_key) ?? _defaultColor;
  }

  Future<void> setColor(int argb) async {
    state = argb;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setInt(_key, argb);
  }
}

final accentColorProvider = NotifierProvider<AccentColorNotifier, int>(
  AccentColorNotifier.new,
);
