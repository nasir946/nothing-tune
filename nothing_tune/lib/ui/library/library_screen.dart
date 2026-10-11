import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:permission_handler/permission_handler.dart';

import '../../providers/library_provider.dart';
import '../../providers/audio_provider.dart';
import '../../providers/app_name_provider.dart';
import '../../models/song.dart';
import '../widgets/mini_player.dart';
import 'songs_tab.dart';
import 'albums_tab.dart';
import 'artists_tab.dart';
import 'genres_tab.dart';
import 'playlists_tab.dart';
import 'audiobooks_tab.dart';
import 'filter_bar.dart';

class LibraryScreen extends ConsumerStatefulWidget {
  const LibraryScreen({super.key});

  @override
  ConsumerState<LibraryScreen> createState() => _LibraryScreenState();
}

class _LibraryScreenState extends ConsumerState<LibraryScreen>
    with SingleTickerProviderStateMixin {
  late final TabController _tabs;
  bool _permissionGranted = false;

  static const _tabLabels = [
    'Songs', 'Albums', 'Artists', 'Genres', 'Playlists', 'Books',
  ];

  @override
  void initState() {
    super.initState();
    _tabs = TabController(length: _tabLabels.length, vsync: this);
    _requestPermissions();
  }

  Future<void> _requestPermissions() async {
    // Android 13+ uses READ_MEDIA_AUDIO; older uses READ_EXTERNAL_STORAGE
    final status = await Permission.audio.request();
    if (!status.isGranted) {
      await Permission.storage.request();
    }
    final granted =
        await Permission.audio.isGranted || await Permission.storage.isGranted;
    if (mounted) setState(() => _permissionGranted = granted);
    if (granted) {
      ref.read(scanProvider.notifier).scanLibrary();
    }
  }

  @override
  void dispose() {
    _tabs.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final scanState = ref.watch(scanProvider);
    final cs = Theme.of(context).colorScheme;

    return Scaffold(
      appBar: AppBar(
        title: Text(ref.watch(appNameProvider).toUpperCase()),
        actions: [
          IconButton(
            icon: const Icon(Icons.search),
            onPressed: () => showSearch(
              context: context,
              delegate: _SongSearchDelegate(ref),
            ),
          ),
          IconButton(
            icon: const Icon(Icons.cloud_download_outlined),
            onPressed: () => context.push('/url-paste'),
            tooltip: 'Stream / Download',
          ),
          IconButton(
            icon: const Icon(Icons.download_outlined),
            onPressed: () => context.push('/downloads'),
            tooltip: 'Download queue',
          ),
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: scanState.isScanning
                ? null
                : () => ref.read(scanProvider.notifier).scanLibrary(),
            tooltip: 'Re-scan library',
          ),
        ],
        bottom: TabBar(
          controller: _tabs,
          isScrollable: true,
          labelColor: cs.primary,
          unselectedLabelColor: cs.onSurface.withOpacity(0.5),
          indicatorColor: cs.primary,
          tabs: _tabLabels.map((l) => Tab(text: l)).toList(),
        ),
      ),
      body: Column(
        children: [
          // Scan progress banner
          if (scanState.isScanning)
            LinearProgressIndicator(
              value: scanState.total > 0
                  ? scanState.current / scanState.total
                  : null,
              color: cs.primary,
              backgroundColor: cs.outline,
            ),

          // Permission denied
          if (!_permissionGranted)
            Expanded(
              child: Center(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Icon(Icons.folder_off, size: 64),
                    const SizedBox(height: 16),
                    const Text('Storage permission required'),
                    const SizedBox(height: 8),
                    TextButton(
                      onPressed: _requestPermissions,
                      child: const Text('Grant permission'),
                    ),
                  ],
                ),
              ),
            )
          else ...[
            // Quality filter chips
            const FilterBar(),

            // Tab views
            Expanded(
              child: TabBarView(
                controller: _tabs,
                children: const [
                  SongsTab(),
                  AlbumsTab(),
                  ArtistsTab(),
                  GenresTab(),
                  PlaylistsTab(),
                  AudiobooksTab(),
                ],
              ),
            ),
          ],

          // Mini player
          const MiniPlayer(),
        ],
      ),
    );
  }
}

// ── Search delegate ────────────────────────────────────────────────────────

class _SongSearchDelegate extends SearchDelegate<Song?> {
  final WidgetRef ref;
  _SongSearchDelegate(this.ref);

  @override
  ThemeData appBarTheme(BuildContext context) => Theme.of(context);

  @override
  List<Widget> buildActions(BuildContext context) => [
        IconButton(
          icon: const Icon(Icons.clear),
          onPressed: () => query = '',
        )
      ];

  @override
  Widget buildLeading(BuildContext context) => IconButton(
        icon: const Icon(Icons.arrow_back),
        onPressed: () => close(context, null),
      );

  @override
  Widget buildResults(BuildContext context) => _buildList(context);

  @override
  Widget buildSuggestions(BuildContext context) => _buildList(context);

  Widget _buildList(BuildContext context) {
    ref.read(searchQueryProvider.notifier).state = query;
    final results = ref.watch(searchResultsProvider);
    return results.when(
      data: (songs) => ListView.builder(
        itemCount: songs.length,
        itemBuilder: (ctx, i) {
          final s = songs[i];
          return ListTile(
            leading: const Icon(Icons.music_note),
            title: Text(s.title),
            subtitle: Text('${s.artist} — ${s.albumTitle}'),
            onTap: () {
              ref.read(audioPlayerServiceProvider).playSong(s, queue: songs, index: i);
              close(context, s);
              context.push('/player');
            },
          );
        },
      ),
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (e, _) => Center(child: Text('Error: $e')),
    );
  }
}
