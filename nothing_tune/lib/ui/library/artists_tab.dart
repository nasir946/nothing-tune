import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../providers/library_provider.dart';
import '../../providers/audio_provider.dart';
import '../../data/database/isar_service.dart';

class ArtistsTab extends ConsumerWidget {
  const ArtistsTab({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final artistsAsync = ref.watch(artistsProvider);
    return artistsAsync.when(
      data: (artists) {
        if (artists.isEmpty) return const Center(child: Text('No artists found.'));
        return ListView.builder(
          itemCount: artists.length,
          itemBuilder: (ctx, i) {
            final artist = artists[i];
            return ListTile(
              leading: CircleAvatar(
                backgroundColor: Theme.of(context).colorScheme.outline,
                child: Text(
                  artist.name.isNotEmpty ? artist.name[0].toUpperCase() : '?',
                  style: const TextStyle(fontWeight: FontWeight.bold),
                ),
              ),
              title: Text(artist.name),
              subtitle: Text('${artist.songCount} songs'),
              onTap: () async {
                final songs = await IsarService.instance.getSongsByArtist(artist.id);
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
