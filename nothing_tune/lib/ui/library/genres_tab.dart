import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../providers/library_provider.dart';
import '../../providers/audio_provider.dart';
import '../../data/database/isar_service.dart';
import '../../models/song.dart';

class GenresTab extends ConsumerWidget {
  const GenresTab({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final genresAsync = ref.watch(genresProvider);
    return genresAsync.when(
      data: (genres) {
        if (genres.isEmpty) return const Center(child: Text('No genres found.'));
        return ListView.builder(
          itemCount: genres.length,
          itemBuilder: (ctx, i) {
            final g = genres[i];
            return ListTile(
              leading: const Icon(Icons.category_outlined),
              title: Text(g.name),
              subtitle: Text('${g.songCount} songs'),
              onTap: () async {
                final songs = await IsarService.instance
                    .searchSongs(g.name);
                if (songs.isEmpty) return;
                ref.read(audioPlayerServiceProvider).playQueue(songs);
                ctx.push('/player');
              },
            );
          },
        );
      },
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (e, _) => Center(child: Text('Error: $e')),
    );
  }
}
