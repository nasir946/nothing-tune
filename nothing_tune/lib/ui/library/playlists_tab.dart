import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../providers/library_provider.dart';
import '../../providers/audio_provider.dart';
import '../../data/database/isar_service.dart';
import '../../models/playlist.dart';

class PlaylistsTab extends ConsumerWidget {
  const PlaylistsTab({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final playlistsAsync = ref.watch(playlistsProvider);
    return Scaffold(
      floatingActionButton: FloatingActionButton(
        heroTag: 'createPlaylist',
        onPressed: () => _showCreateDialog(context, ref),
        child: const Icon(Icons.add),
      ),
      body: playlistsAsync.when(
        data: (playlists) {
          // Smart playlists always shown at top
          final smarts = [
            ('Recently Added', Icons.new_releases_outlined, 'recent'),
            ('Most Played', Icons.trending_up, 'mostPlayed'),
            ('Unplayed', Icons.fiber_new, 'unplayed'),
            ('Favorites', Icons.favorite_outline, 'favorites'),
          ];

          final total = smarts.length + playlists.length;
          return ListView.builder(
            itemCount: total,
            itemBuilder: (ctx, i) {
              if (i < smarts.length) {
                final (name, icon, type) = smarts[i];
                return ListTile(
                  leading: Icon(icon),
                  title: Text(name),
                  onTap: () async {
                    final songs = await _loadSmart(type);
                    ref.read(audioPlayerServiceProvider).playQueue(songs);
                    ctx.push('/player');
                  },
                );
              }
              final p = playlists[i - smarts.length];
              return ListTile(
                leading: const Icon(Icons.queue_music),
                title: Text(p.name),
                subtitle: Text('${p.songIds.length} songs'),
                onTap: () async {
                  final songs = await IsarService.instance.getSongsForPlaylist(p);
                  ref.read(audioPlayerServiceProvider).playQueue(songs);
                  ctx.push('/player');
                },
                trailing: IconButton(
                  icon: const Icon(Icons.delete_outline),
                  onPressed: () async {
                    await IsarService.instance.deletePlaylist(p.id);
                    ref.invalidate(playlistsProvider);
                  },
                ),
              );
            },
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('Error: $e')),
      ),
    );
  }

  Future<List> _loadSmart(String type) {
    switch (type) {
      case 'recent':
        return IsarService.instance.getRecentlyAdded();
      case 'mostPlayed':
        return IsarService.instance.getMostPlayed();
      case 'unplayed':
        return IsarService.instance.getUnplayed();
      case 'favorites':
        return IsarService.instance.getFavorites();
      default:
        return Future.value([]);
    }
  }

  void _showCreateDialog(BuildContext context, WidgetRef ref) {
    final controller = TextEditingController();
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('New Playlist'),
        content: TextField(
          controller: controller,
          autofocus: true,
          decoration: const InputDecoration(hintText: 'Playlist name'),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
          TextButton(
            onPressed: () async {
              if (controller.text.trim().isEmpty) return;
              final p = Playlist()
                ..name = controller.text.trim()
                ..createdMs = DateTime.now().millisecondsSinceEpoch
                ..modifiedMs = DateTime.now().millisecondsSinceEpoch;
              await IsarService.instance.savePlaylist(p);
              ref.invalidate(playlistsProvider);
              if (ctx.mounted) Navigator.pop(ctx);
            },
            child: const Text('Create'),
          ),
        ],
      ),
    );
  }
}
