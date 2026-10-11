class FormatUtils {
  FormatUtils._();

  /// Format a Duration as mm:ss or h:mm:ss
  static String duration(Duration d) {
    final h = d.inHours;
    final m = d.inMinutes.remainder(60).toString().padLeft(2, '0');
    final s = d.inSeconds.remainder(60).toString().padLeft(2, '0');
    return h > 0 ? '$h:$m:$s' : '$m:$s';
  }

  /// Human-readable file size
  static String fileSize(int bytes) {
    if (bytes < 1024) return '${bytes}B';
    if (bytes < 1024 * 1024) return '${(bytes / 1024).toStringAsFixed(1)}KB';
    if (bytes < 1024 * 1024 * 1024) {
      return '${(bytes / (1024 * 1024)).toStringAsFixed(1)}MB';
    }
    return '${(bytes / (1024 * 1024 * 1024)).toStringAsFixed(2)}GB';
  }

  /// Format bitrate: 192000 → "192 kHz", 320000 (bps) → "320 kbps"
  static String sampleRate(int hz) {
    if (hz >= 1000) return '${(hz / 1000).toStringAsFixed(0)} kHz';
    return '${hz} Hz';
  }

  static String bitrate(int kbps) => '${kbps} kbps';
  static String bitDepth(int bits) => '${bits}-bit';
}
