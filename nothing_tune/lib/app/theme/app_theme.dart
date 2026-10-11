import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import '../../providers/theme_provider.dart';

class AppTheme {
  AppTheme._();

  static TextTheme get _spaceMono => GoogleFonts.spaceMonoTextTheme();

  // ── Dot Mono (pure black / white) ─────────────────────────────────────────
  static ThemeData dotMono() {
    return ThemeData(
      brightness: Brightness.dark,
      scaffoldBackgroundColor: Colors.black,
      colorScheme: const ColorScheme.dark(
        surface: Colors.black,
        onSurface: Colors.white,
        primary: Colors.white,
        onPrimary: Colors.black,
        secondary: Color(0xFFCCCCCC),
        onSecondary: Colors.black,
        outline: Color(0xFF333333),
      ),
      textTheme: _spaceMono.apply(
        bodyColor: Colors.white,
        displayColor: Colors.white,
      ),
      iconTheme: const IconThemeData(color: Colors.white),
      dividerColor: const Color(0xFF222222),
      appBarTheme: AppBarTheme(
        backgroundColor: Colors.black,
        foregroundColor: Colors.white,
        elevation: 0,
        titleTextStyle: GoogleFonts.spaceMono(
          color: Colors.white,
          fontSize: 18,
          fontWeight: FontWeight.bold,
          letterSpacing: 2,
        ),
      ),
      bottomNavigationBarTheme: const BottomNavigationBarThemeData(
        backgroundColor: Colors.black,
        selectedItemColor: Colors.white,
        unselectedItemColor: Color(0xFF555555),
      ),
      sliderTheme: SliderThemeData(
        activeTrackColor: Colors.white,
        inactiveTrackColor: const Color(0xFF333333),
        thumbColor: Colors.white,
        overlayColor: Colors.white.withOpacity(0.12),
      ),
      progressIndicatorTheme: const ProgressIndicatorThemeData(
        color: Colors.white,
      ),
      chipTheme: const ChipThemeData(
        backgroundColor: Color(0xFF111111),
        labelStyle: TextStyle(color: Colors.white),
        side: BorderSide(color: Color(0xFF444444)),
      ),
    );
  }

  // ── Dot + Accent (black bg + user accent color) ────────────────────────────
  static ThemeData dotAccent(Color accent) {
    final base = dotMono();
    return base.copyWith(
      colorScheme: ColorScheme.dark(
        surface: Colors.black,
        onSurface: Colors.white,
        primary: accent,
        onPrimary: _contrastColor(accent),
        secondary: accent.withOpacity(0.7),
        onSecondary: _contrastColor(accent),
        outline: const Color(0xFF333333),
      ),
      iconTheme: IconThemeData(color: accent),
      appBarTheme: base.appBarTheme.copyWith(foregroundColor: accent),
      sliderTheme: base.sliderTheme.copyWith(
        activeTrackColor: accent,
        thumbColor: accent,
        overlayColor: accent.withOpacity(0.12),
      ),
      bottomNavigationBarTheme: base.bottomNavigationBarTheme.copyWith(
        selectedItemColor: accent,
      ),
    );
  }

  // ── Glassmorphism (dark blur cards) ───────────────────────────────────────
  static ThemeData glass(Color accent) {
    return ThemeData(
      brightness: Brightness.dark,
      scaffoldBackgroundColor: const Color(0xFF0A0A0F),
      colorScheme: ColorScheme.dark(
        surface: Colors.white.withOpacity(0.05),
        onSurface: Colors.white,
        primary: accent,
        onPrimary: _contrastColor(accent),
        secondary: accent.withOpacity(0.6),
        onSecondary: _contrastColor(accent),
        outline: Colors.white.withOpacity(0.15),
      ),
      textTheme: _spaceMono.apply(
        bodyColor: Colors.white,
        displayColor: Colors.white,
      ),
      iconTheme: const IconThemeData(color: Colors.white),
      appBarTheme: AppBarTheme(
        backgroundColor: Colors.transparent,
        foregroundColor: Colors.white,
        elevation: 0,
        titleTextStyle: GoogleFonts.spaceMono(
          color: Colors.white,
          fontSize: 18,
          fontWeight: FontWeight.bold,
          letterSpacing: 2,
        ),
      ),
      sliderTheme: SliderThemeData(
        activeTrackColor: accent,
        inactiveTrackColor: Colors.white.withOpacity(0.2),
        thumbColor: accent,
        overlayColor: accent.withOpacity(0.12),
      ),
      bottomNavigationBarTheme: BottomNavigationBarThemeData(
        backgroundColor: Colors.white.withOpacity(0.05),
        selectedItemColor: accent,
        unselectedItemColor: Colors.white.withOpacity(0.4),
      ),
    );
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  static Color _contrastColor(Color c) {
    final luminance = c.computeLuminance();
    return luminance > 0.4 ? Colors.black : Colors.white;
  }

  static ThemeData resolve(AppThemeMode mode, Color accent) {
    switch (mode) {
      case AppThemeMode.dotMono:
        return dotMono();
      case AppThemeMode.dotAccent:
        return dotAccent(accent);
      case AppThemeMode.glass:
        return glass(accent);
    }
  }
}
