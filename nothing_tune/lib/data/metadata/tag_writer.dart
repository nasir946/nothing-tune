import 'dart:io';
import 'package:metadata_god/metadata_god.dart';
import '../../models/song.dart';
import '../database/isar_service.dart';

class TagWriter {
  TagWriter._();
  static final TagWriter instance = TagWriter._();

  bool _initialized = false;

  Future<void> init() async {
    if (_initialized) return;
    try {
      await MetadataGod.initialize();
      _initialized = true;
    } catch (_) {}
  }

  /// Write updated metadata to the audio file on disk and update the local database.
  Future<bool> updateSongMetadata({
    required Song song,
    String? newTitle,
    String? newArtist,
    String? newAlbum,
    String? newGenre,
    int? newTrackNumber,
    int? newYear,
  }) async {
    final file = File(song.path);
    if (!file.existsSync()) return false;

    await init();

    try {
      final meta = Metadata(
        title: newTitle ?? song.title,
        artist: newArtist ?? song.artist,
        album: newAlbum ?? song.albumTitle,
        genre: newGenre ?? song.genre,
        trackNumber: newTrackNumber ?? song.trackNumber,
        year: newYear ?? song.year,
      );

      await MetadataGod.writeMetadata(
        file: song.path,
        metadata: meta,
      );

      // Update in memory and database
      if (newTitle != null) song.title = newTitle;
      if (newArtist != null) song.artist = newArtist;
      if (newAlbum != null) song.albumTitle = newAlbum;
      if (newGenre != null) song.genre = newGenre;
      if (newTrackNumber != null) song.trackNumber = newTrackNumber;
      if (newYear != null) song.year = newYear;

      await IsarService.instance.updateSong(song);
      return true;
    } catch (_) {
      return false;
    }
  }
}
