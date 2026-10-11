import 'package:isar/isar.dart';
import 'package:path_provider/path_provider.dart';

import '../../models/song.dart';
import '../../models/album.dart';
import '../../models/artist.dart';
import '../../models/genre.dart';
import '../../models/playlist.dart';
import '../../models/radio_station.dart';
import '../../models/download_task.dart';

/// Singleton wrapper around the Isar database instance.
class IsarService {
  IsarService._();
  static final IsarService instance = IsarService._();

  late final Isar _isar;
  bool _initialized = false;

  Future<void> init() async {
    if (_initialized) return;
    final dir = await getApplicationDocumentsDirectory();
    _isar = await Isar.open(
      [
        SongSchema,
        AlbumSchema,
        ArtistSchema,
        GenreSchema,
        PlaylistSchema,
        RadioStationSchema,
        DownloadTaskSchema,
      ],
      directory: dir.path,
    );
    _initialized = true;
  }

  Isar get db => _isar;

  // ── Songs ──────────────────────────────────────────────────────────────────

  Future<List<Song>> getAllSongs() => _isar.songs.where().findAll();

  Future<List<Song>> getSongsByAlbum(int albumId) =>
      _isar.songs.filter().albumIdEqualTo(albumId).findAll();

  Future<List<Song>> getSongsByArtist(int artistId) =>
      _isar.songs.filter().artistIdEqualTo(artistId).findAll();

  Future<List<Song>> getSongsByQuality(AudioQuality q) =>
      _isar.songs.filter().qualityEqualTo(q).findAll();

  Future<List<Song>> getAudiobooks() =>
      _isar.songs.filter().isAudiobookEqualTo(true).findAll();

  Future<List<Song>> getFavorites() =>
      _isar.songs.filter().isFavoriteEqualTo(true).findAll();

  Future<List<Song>> getRecentlyAdded({int limit = 50}) => _isar.songs
      .where()
      .sortByAddedMsDesc()
      .limit(limit)
      .findAll();

  Future<List<Song>> getMostPlayed({int limit = 50}) => _isar.songs
      .where()
      .sortByPlayCountDesc()
      .limit(limit)
      .findAll();

  Future<List<Song>> getUnplayed() =>
      _isar.songs.filter().playCountEqualTo(0).findAll();

  Future<List<Song>> searchSongs(String query) {
    final q = query.toLowerCase();
    return _isar.songs
        .filter()
        .titleContains(q, caseSensitive: false)
        .or()
        .artistContains(q, caseSensitive: false)
        .or()
        .albumTitleContains(q, caseSensitive: false)
        .findAll();
  }

  Future<void> upsertSongs(List<Song> songs) async {
    await _isar.writeTxn(() async {
      await _isar.songs.putAll(songs);
    });
  }

  Future<void> updateSong(Song song) async {
    await _isar.writeTxn(() async {
      await _isar.songs.put(song);
    });
  }

  Future<void> incrementPlayCount(int songId) async {
    final song = await _isar.songs.get(songId);
    if (song == null) return;
    song.playCount++;
    song.lastPlayedMs = DateTime.now().millisecondsSinceEpoch;
    await _isar.writeTxn(() async => _isar.songs.put(song));
  }

  // ── Albums ─────────────────────────────────────────────────────────────────

  Future<List<Album>> getAllAlbums() => _isar.albums.where().findAll();

  Future<Album?> getAlbumByTitle(String title) =>
      _isar.albums.filter().titleEqualTo(title).findFirst();

  Future<void> upsertAlbums(List<Album> albums) async {
    await _isar.writeTxn(() async => _isar.albums.putAll(albums));
  }

  // ── Artists ────────────────────────────────────────────────────────────────

  Future<List<Artist>> getAllArtists() => _isar.artists.where().findAll();

  Future<Artist?> getArtistByName(String name) =>
      _isar.artists.filter().nameEqualTo(name).findFirst();

  Future<void> upsertArtists(List<Artist> artists) async {
    await _isar.writeTxn(() async => _isar.artists.putAll(artists));
  }

  // ── Genres ─────────────────────────────────────────────────────────────────

  Future<List<Genre>> getAllGenres() => _isar.genres.where().findAll();

  Future<void> upsertGenres(List<Genre> genres) async {
    await _isar.writeTxn(() async => _isar.genres.putAll(genres));
  }

  // ── Playlists ──────────────────────────────────────────────────────────────

  Future<List<Playlist>> getAllPlaylists() => _isar.playlists.where().findAll();

  Future<void> savePlaylist(Playlist playlist) async {
    playlist.modifiedMs = DateTime.now().millisecondsSinceEpoch;
    await _isar.writeTxn(() async => _isar.playlists.put(playlist));
  }

  Future<void> deletePlaylist(int id) async {
    await _isar.writeTxn(() async => _isar.playlists.delete(id));
  }

  Future<List<Song>> getSongsForPlaylist(Playlist playlist) async {
    final songs = <Song>[];
    for (final sid in playlist.songIds) {
      final s = await _isar.songs.get(sid);
      if (s != null) songs.add(s);
    }
    return songs;
  }

  // ── Radio stations ─────────────────────────────────────────────────────────

  Future<List<RadioStation>> getAllStations() =>
      _isar.radioStations.where().findAll();

  Future<void> saveStation(RadioStation station) async {
    await _isar.writeTxn(() async => _isar.radioStations.put(station));
  }

  Future<void> deleteStation(int id) async {
    await _isar.writeTxn(() async => _isar.radioStations.delete(id));
  }

  // ── Download tasks ─────────────────────────────────────────────────────────

  Future<List<DownloadTask>> getAllDownloadTasks() =>
      _isar.downloadTasks.where().findAll();

  Future<void> saveDownloadTask(DownloadTask task) async {
    await _isar.writeTxn(() async => _isar.downloadTasks.put(task));
  }

  Future<void> deleteDownloadTask(int id) async {
    await _isar.writeTxn(() async => _isar.downloadTasks.delete(id));
  }
}
