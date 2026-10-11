import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../providers/library_provider.dart';
import '../../providers/audio_provider.dart';
import '../../data/database/isar_service.dart';
import '../widgets/album_art.dart';

class AlbumsTab extends ConsumerWidget {
  const AlbumsTab({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final albumsAsync = ref.watch(albumsProvider);
    return albumsAsync.when(
      data: (albums) {
        if (albums.isEmpty) return const Center(child: Text('No albums found.'));
        return LayoutBuilder(
          builder: (context, constraints) {
            // Adaptive column count for One UI multi-window / tablets
            final crossAxisCount = (constraints.maxWidth / 160).floor().clamp(2, 6);

            return GridView.builder(
              padding: const EdgeInsets.all(12),
              gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: crossAxisCount,
                mainAxisSpacing: 12,
                crossAxisSpacing: 12,
                childAspectRatio: 0.80,
              ),
              itemCount: albums.length,
              itemBuilder: (ctx, i) {
                final album = albums[i];
                return InkWell(
                  borderRadius: BorderRadius.circular(12),
                  onTap: () async {
                    final songs = await IsarService.instance.getSongsByAlbum(album.id);
                    if (songs.isEmpty) return;
                    ref.read(audioPlayerServiceProvider).playQueue(songs);
                    ctx.push('/player');
                  },
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Expanded(
                        child: ClipRRect(
                          borderRadius: BorderRadius.circular(12),
                          child: AlbumArtWidget(
                            artPath: album.artPath,
                            size: double.infinity,
                          ),
                        ),
                      ),
                      const SizedBox(height: 6),
                      Text(
                        album.title,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 12),
                      ),
                      Text(
                        album.artist,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: TextStyle(
                          fontSize: 11,
                          color: Theme.of(context).colorScheme.onSurface.withOpacity(0.6),
                        ),
                      ),
                    ],
                  ),
                );
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
