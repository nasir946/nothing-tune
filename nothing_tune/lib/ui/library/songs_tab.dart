import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../providers/library_provider.dart';
import '../../providers/audio_provider.dart';
import '../../models/song.dart';
import '../../utils/format_utils.dart';
import '../widgets/album_art.dart';
import '../widgets/song_tile.dart';

class SongsTab extends ConsumerWidget {
  const SongsTab({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final songsAsync = ref.watch(filteredSongsProvider);
    return songsAsync.when(
      data: (songs) {
        if (songs.isEmpty) {
          return const Center(child: Text('No songs found.\nTap refresh to scan.'));
        }
        return ListView.builder(
          itemCount: songs.length,
          itemBuilder: (ctx, i) => SongTile(
            song: songs[i],
            onTap: () {
              ref
                  .read(audioPlayerServiceProvider)
                  .playSong(songs[i], queue: songs, index: i);
              context.push('/player');
            },
          ),
        );
      },
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (e, _) => Center(child: Text('Error: $e')),
    );
  }
}
