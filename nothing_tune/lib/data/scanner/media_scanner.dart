import 'dart:io';
import 'package:flutter/services.dart';
import 'package:path_provider/path_provider.dart';

import '../../models/song.dart';
import '../../models/album.dart';
import '../../models/artist.dart';
import '../../models/genre.dart';
import '../database/isar_service.dart';

/// Calls the native MediaStorePlugin via MethodChannel and populates Isar.
class MediaScanner {
  static const _channel = MethodChannel('com.nothing.tune/media_store');

  /// Scan the device and return count of songs found.
  Future<int> scanLibrary({void Function(int current, int total)? onProgress}) async {
    // 1 — ask native side for raw song maps
    final List<dynamic> raw =
        await _channel.invokeMethod('scanMusic') as List<dynamic>;

    final db = IsarService.instance;
    final albumMap = <String, Album>{};
    final artistMap = <String, Artist>{};
    final genreMap = <String, Genre>{};
    final songs = <Song>[];

    final total = raw.length;
    int current = 0;

    for (final item in raw) {
      final map = Map<String, dynamic>.from(item as Map);

      // ── Build Song ───────────────────────────────────────────────────────
      final song = Song()
        ..path = (map['path'] as String?) ?? ''
        ..title = (map['title'] as String?) ?? 'Unknown'
        ..artist = (map['artist'] as String?) ?? 'Unknown Artist'
        ..albumTitle = (map['album'] as String?) ?? 'Unknown Album'
        ..genre = (map['genre'] as String?) ?? 'Unknown'
        ..trackNumber = map['track'] as int?
        ..discNumber = map['disc'] as int?
        ..year = map['year'] as int?
        ..durationMs = map['duration'] as int?
        ..bitrate = map['bitrate'] as int?
        ..sampleRate = map['sampleRate'] as int?
        ..bitDepth = map['bitDepth'] as int?
        ..codec = map['codec'] as String?
        ..addedMs = (map['dateAdded'] as int?) ?? DateTime.now().millisecondsSinceEpoch
        ..embeddedArtPath = map['artPath'] as String?;

      // ── Quality tier ─────────────────────────────────────────────────────
      song.quality = _detectQuality(song);

      // ── Audiobook detection ───────────────────────────────────────────────
      final genreLower = song.genre.toLowerCase();
      if (genreLower.contains('audiobook') ||
          genreLower.contains('podcast') ||
          genreLower.contains('spoken')) {
        song.isAudiobook = true;
      }

      songs.add(song);

      // ── Album ─────────────────────────────────────────────────────────────
      if (!albumMap.containsKey(song.albumTitle)) {
        albumMap[song.albumTitle] = Album()
          ..title = song.albumTitle
          ..artist = song.artist
          ..year = song.year
          ..artPath = song.embeddedArtPath
          ..songCount = 1;
      } else {
        albumMap[song.albumTitle]!.songCount++;
      }

      // ── Artist ────────────────────────────────────────────────────────────
      if (!artistMap.containsKey(song.artist)) {
        artistMap[song.artist] = Artist()
          ..name = song.artist
          ..songCount = 1;
      } else {
        artistMap[song.artist]!.songCount++;
      }

      // ── Genre ─────────────────────────────────────────────────────────────
      if (!genreMap.containsKey(song.genre)) {
        genreMap[song.genre] = Genre()
          ..name = song.genre
          ..songCount = 1;
      } else {
        genreMap[song.genre]!.songCount++;
      }

      current++;
      onProgress?.call(current, total);
    }

    // Persist albums & artists first so we can assign IDs to songs
    await db.upsertAlbums(albumMap.values.toList());
    await db.upsertArtists(artistMap.values.toList());
    await db.upsertGenres(genreMap.values.toList());

    // Assign album/artist IDs to songs
    for (final song in songs) {
      final album = await db.getAlbumByTitle(song.albumTitle);
      if (album != null) song.albumId = album.id;
      final artist = await db.getArtistByName(song.artist);
      if (artist != null) song.artistId = artist.id;
    }

    await db.upsertSongs(songs);
    return songs.length;
  }

  AudioQuality _detectQuality(Song song) {
    final codec = (song.codec ?? '').toUpperCase();
    final bd = song.bitDepth ?? 16;
    final sr = song.sampleRate ?? 44100;

    if (bd >= 24 || sr > 48000) return AudioQuality.hires;
    if (codec == 'FLAC' || codec == 'ALAC' || codec == 'WAV' ||
        codec == 'AIFF' || codec == 'APE' || codec == 'TTA') {
      return AudioQuality.lossless;
    }
    return AudioQuality.standard;
  }
}
