import 'dart:convert';
import 'package:crypto/crypto.dart';
import 'package:dio/dio.dart';

import '../../config/app_config.dart';
import '../../models/song.dart';
import 'lastfm_auth.dart';

/// Sends scrobbles and "Now Playing" updates to Last.fm.
class LastFmScrobbler {
  LastFmScrobbler._();
  static final LastFmScrobbler instance = LastFmScrobbler._();

  static const _baseUrl = 'https://ws.audioscrobbler.com/2.0/';
  final _dio = Dio();

  // ── Scrobble ───────────────────────────────────────────────────────────────

  Future<void> scrobble(Song song) async {
    final sessionKey = await LastFmAuth.instance.getSessionKey();
    if (sessionKey == null || AppConfig.lastFmApiKey.isEmpty) return;

    final timestamp =
        (DateTime.now().millisecondsSinceEpoch ~/ 1000).toString();

    final params = {
      'method': 'track.scrobble',
      'artist': song.artist,
      'track': song.title,
      'album': song.albumTitle,
      'timestamp': timestamp,
      'api_key': AppConfig.lastFmApiKey,
      'sk': sessionKey,
    };

    params['api_sig'] = _sign(params);
    params['format'] = 'json';

    try {
      await _dio.post(_baseUrl, data: params);
    } catch (_) {}
  }

  // ── Now Playing ────────────────────────────────────────────────────────────

  Future<void> updateNowPlaying(Song song) async {
    final sessionKey = await LastFmAuth.instance.getSessionKey();
    if (sessionKey == null || AppConfig.lastFmApiKey.isEmpty) return;

    final params = {
      'method': 'track.updateNowPlaying',
      'artist': song.artist,
      'track': song.title,
      'album': song.albumTitle,
      'duration': ((song.durationMs ?? 0) ~/ 1000).toString(),
      'api_key': AppConfig.lastFmApiKey,
      'sk': sessionKey,
    };

    params['api_sig'] = _sign(params);
    params['format'] = 'json';

    try {
      await _dio.post(_baseUrl, data: params);
    } catch (_) {}
  }

  // ── Signature ──────────────────────────────────────────────────────────────

  String _sign(Map<String, String> params) {
    // Remove 'format' and 'callback' keys if present
    final filtered = Map<String, String>.from(params)
      ..remove('format')
      ..remove('callback');

    final sorted = filtered.keys.toList()..sort();
    final sigStr = sorted.map((k) => '$k${filtered[k]}').join();
    final toHash = sigStr + AppConfig.lastFmSharedSecret;
    return md5.convert(utf8.encode(toHash)).toString();
  }
}
