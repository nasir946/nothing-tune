import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../data/database/isar_service.dart';
import '../data/scanner/media_scanner.dart';
import '../models/song.dart';
import '../models/album.dart';
import '../models/artist.dart';
import '../models/genre.dart';
import '../models/playlist.dart';

// ── Scan state ─────────────────────────────────────────────────────────────

class ScanState {
  final bool isScanning;
  final int current;
  final int total;
  ScanState({required this.isScanning, this.current = 0, this.total = 0});
}

class ScanNotifier extends Notifier<ScanState> {
  @override
  ScanState build() => ScanState(isScanning: false);

  Future<void> scanLibrary() async {
    state = ScanState(isScanning: true);
    final scanner = MediaScanner();
    await scanner.scanLibrary(
      onProgress: (cur, tot) {
        state = ScanState(isScanning: true, current: cur, total: tot);
      },
    );
    state = ScanState(isScanning: false);
    // Invalidate all library providers
    ref.invalidate(songsProvider);
    ref.invalidate(albumsProvider);
    ref.invalidate(artistsProvider);
    ref.invalidate(genresProvider);
  }
}

final scanProvider = NotifierProvider<ScanNotifier, ScanState>(ScanNotifier.new);

// ── Library data providers ─────────────────────────────────────────────────

final songsProvider = FutureProvider<List<Song>>(
  (_) => IsarService.instance.getAllSongs(),
);

final albumsProvider = FutureProvider<List<Album>>(
  (_) => IsarService.instance.getAllAlbums(),
);

final artistsProvider = FutureProvider<List<Artist>>(
  (_) => IsarService.instance.getAllArtists(),
);

final genresProvider = FutureProvider<List<Genre>>(
  (_) => IsarService.instance.getAllGenres(),
);

final playlistsProvider = FutureProvider<List<Playlist>>(
  (_) => IsarService.instance.getAllPlaylists(),
);

final audiobooksProvider = FutureProvider<List<Song>>(
  (_) => IsarService.instance.getAudiobooks(),
);

final favoritesProvider = FutureProvider<List<Song>>(
  (_) => IsarService.instance.getFavorites(),
);

// ── Filtered / smart playlists ─────────────────────────────────────────────

final recentlyAddedProvider = FutureProvider<List<Song>>(
  (_) => IsarService.instance.getRecentlyAdded(),
);

final mostPlayedProvider = FutureProvider<List<Song>>(
  (_) => IsarService.instance.getMostPlayed(),
);

final unplayedProvider = FutureProvider<List<Song>>(
  (_) => IsarService.instance.getUnplayed(),
);

// ── Quality filter ─────────────────────────────────────────────────────────

final qualityFilterProvider = StateProvider<AudioQuality?>((_) => null);

final filteredSongsProvider = FutureProvider<List<Song>>((ref) async {
  final quality = ref.watch(qualityFilterProvider);
  if (quality == null) return IsarService.instance.getAllSongs();
  return IsarService.instance.getSongsByQuality(quality);
});

// ── Search ────────────────────────────────────────────────────────────────

final searchQueryProvider = StateProvider<String>((_) => '');

final searchResultsProvider = FutureProvider<List<Song>>((ref) {
  final q = ref.watch(searchQueryProvider);
  if (q.isEmpty) return Future.value([]);
  return IsarService.instance.searchSongs(q);
});
