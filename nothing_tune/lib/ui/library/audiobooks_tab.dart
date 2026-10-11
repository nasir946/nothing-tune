import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../providers/library_provider.dart';
import '../../providers/audio_provider.dart';
import '../../utils/format_utils.dart';
import '../widgets/song_tile.dart';

class AudiobooksTab extends ConsumerWidget {
  const AudiobooksTab({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final booksAsync = ref.watch(audiobooksProvider);
    return booksAsync.when(
      data: (books) {
        if (books.isEmpty) {
          return const Center(
            child: Text(
              'No audiobooks found.\nTag your files with genre "Audiobook"\nor mark them in the song options.',
            ),
          );
        }
        return ListView.builder(
          itemCount: books.length,
          itemBuilder: (ctx, i) {
            final b = books[i];
            final progress = b.durationMs != null && b.durationMs! > 0
                ? b.audiobookPositionMs / b.durationMs!
                : 0.0;
            return SongTile(
              song: b,
              subtitle: progress > 0
                  ? '${(progress * 100).toStringAsFixed(0)}% played — '
                      '${FormatUtils.duration(Duration(milliseconds: b.audiobookPositionMs))} left'
                  : null,
              onTap: () {
                ref
                    .read(audioPlayerServiceProvider)
                    .playSong(b, queue: books, index: i);
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
