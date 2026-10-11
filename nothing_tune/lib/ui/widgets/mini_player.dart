import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../providers/audio_provider.dart';
import '../../utils/format_utils.dart';
import 'album_art.dart';
import 'waveform_bars.dart';

/// Persistent mini player shown at the bottom of the main shell.
class MiniPlayer extends ConsumerWidget {
  const MiniPlayer({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final playerAsync = ref.watch(playerStateProvider);

    return playerAsync.when(
      data: (ps) {
        if (ps.currentSong == null) return const SizedBox.shrink();

        final song = ps.currentSong!;
        final cs = Theme.of(context).colorScheme;

        return GestureDetector(
          onTap: () => context.push('/player'),
          child: Container(
            height: 68,
            color: cs.surface.withOpacity(0.95),
            padding: const EdgeInsets.symmetric(horizontal: 12),
            child: Row(
              children: [
                // Art
                ClipRRect(
                  borderRadius: BorderRadius.circular(6),
                  child: AlbumArtWidget(
                    artPath: song.embeddedArtPath,
                    networkUrl: song.onlineArtUrl,
                    size: 44,
                  ),
                ),
                const SizedBox(width: 12),

                // Info + waveform
                Expanded(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        song.title,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: 13,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Row(
                        children: [
                          Expanded(
                            child: Text(
                              song.artist,
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                              style: TextStyle(
                                fontSize: 11,
                                color: cs.onSurface.withOpacity(0.6),
                              ),
                            ),
                          ),
                          const SizedBox(width: 8),
                          SizedBox(
                            width: 40,
                            height: 14,
                            child: WaveformBars(
                              isPlaying: ps.isPlaying,
                              barCount: 12,
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),

                // Controls
                IconButton(
                  icon: const Icon(Icons.skip_previous),
                  onPressed: () =>
                      ref.read(audioPlayerServiceProvider).skipToPrevious(),
                ),
                IconButton(
                  icon: Icon(
                    ps.isPlaying ? Icons.pause : Icons.play_arrow,
                  ),
                  onPressed: () {
                    final svc = ref.read(audioPlayerServiceProvider);
                    ps.isPlaying ? svc.pause() : svc.play();
                  },
                ),
                IconButton(
                  icon: const Icon(Icons.skip_next),
                  onPressed: () =>
                      ref.read(audioPlayerServiceProvider).skipToNext(),
                ),
              ],
            ),
          ),
        );
      },
      loading: () => const SizedBox.shrink(),
      error: (_, __) => const SizedBox.shrink(),
    );
  }
}
