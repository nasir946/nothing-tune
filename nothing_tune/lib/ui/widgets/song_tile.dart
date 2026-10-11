import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../models/song.dart';
import '../../utils/format_utils.dart';
import '../../data/database/isar_service.dart';
import '../../data/metadata/tag_writer.dart';
import '../../providers/audio_provider.dart';
import '../../providers/library_provider.dart';
import 'album_art.dart';

class SongTile extends ConsumerWidget {
  final Song song;
  final VoidCallback onTap;
  final String? subtitle;
  final Widget? trailing;

  const SongTile({
    super.key,
    required this.song,
    required this.onTap,
    this.subtitle,
    this.trailing,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final cs = Theme.of(context).colorScheme;

    // Quality badge color
    Color? badgeColor;
    String? badge;
    switch (song.quality) {
      case AudioQuality.hires:
        badgeColor = Colors.amber;
        badge = 'HI-RES';
      case AudioQuality.lossless:
        badgeColor = Colors.lightGreen;
        badge = 'LOSSLESS';
      case AudioQuality.standard:
        badgeColor = null;
        badge = null;
    }

    return ListTile(
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      leading: AlbumArtWidget(
        artPath: song.embeddedArtPath,
        networkUrl: song.onlineArtUrl,
        size: 48,
      ),
      title: Row(
        children: [
          Expanded(
            child: Text(
              song.title,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 14),
            ),
          ),
          if (song.isAudiobook) ...[
            const SizedBox(width: 6),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 5, vertical: 2),
              decoration: BoxDecoration(
                color: Colors.blueAccent.withOpacity(0.18),
                border: Border.all(color: Colors.blueAccent.withOpacity(0.6)),
                borderRadius: BorderRadius.circular(4),
              ),
              child: const Text(
                'BOOK',
                style: TextStyle(
                  color: Colors.blueAccent,
                  fontSize: 9,
                  fontWeight: FontWeight.bold,
                  letterSpacing: 0.5,
                ),
              ),
            ),
          ],
          if (badge != null) ...[
            const SizedBox(width: 6),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 5, vertical: 2),
              decoration: BoxDecoration(
                color: badgeColor?.withOpacity(0.15),
                border: Border.all(color: badgeColor!.withOpacity(0.6)),
                borderRadius: BorderRadius.circular(4),
              ),
              child: Text(
                badge,
                style: TextStyle(
                  color: badgeColor,
                  fontSize: 9,
                  fontWeight: FontWeight.bold,
                  letterSpacing: 0.5,
                ),
              ),
            ),
          ],
        ],
      ),
      subtitle: Text(
        subtitle ?? '${song.artist} · ${FormatUtils.duration(Duration(milliseconds: song.durationMs ?? 0))}',
        maxLines: 1,
        overflow: TextOverflow.ellipsis,
        style: TextStyle(
          fontSize: 12,
          color: cs.onSurface.withOpacity(0.55),
        ),
      ),
      trailing: trailing ??
          PopupMenuButton<String>(
            icon: Icon(Icons.more_vert, size: 18, color: cs.onSurface.withOpacity(0.5)),
            onSelected: (choice) => _handleAction(context, ref, choice),
            itemBuilder: (ctx) => [
              const PopupMenuItem(
                value: 'queue',
                child: Row(
                  children: [
                    Icon(Icons.queue_music, size: 18),
                    SizedBox(width: 10),
                    Text('Add to queue'),
                  ],
                ),
              ),
              PopupMenuItem(
                value: 'audiobook',
                child: Row(
                  children: [
                    Icon(song.isAudiobook ? Icons.bookmark_remove : Icons.bookmark_add, size: 18),
                    SizedBox(width: 10),
                    Text(song.isAudiobook ? 'Unmark as Audiobook' : 'Mark as Audiobook'),
                  ],
                ),
              ),
              PopupMenuItem(
                value: 'favorite',
                child: Row(
                  children: [
                    Icon(song.isFavorite ? Icons.favorite : Icons.favorite_border, size: 18),
                    SizedBox(width: 10),
                    Text(song.isFavorite ? 'Remove Favorite' : 'Add to Favorites'),
                  ],
                ),
              ),
              const PopupMenuItem(
                value: 'edit',
                child: Row(
                  children: [
                    Icon(Icons.edit_note, size: 18),
                    SizedBox(width: 10),
                    Text('Edit tags'),
                  ],
                ),
              ),
              const PopupMenuItem(
                value: 'info',
                child: Row(
                  children: [
                    Icon(Icons.info_outline, size: 18),
                    SizedBox(width: 10),
                    Text('Audio info'),
                  ],
                ),
              ),
            ],
          ),
      onTap: onTap,
    );
  }

  void _handleAction(BuildContext context, WidgetRef ref, String choice) async {
    switch (choice) {
      case 'queue':
        ref.read(audioPlayerServiceProvider).addToQueue(song);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Added "${song.title}" to queue')),
        );
      case 'audiobook':
        song.isAudiobook = !song.isAudiobook;
        await IsarService.instance.updateSong(song);
        ref.invalidate(audiobooksProvider);
        ref.invalidate(songsProvider);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              song.isAudiobook
                  ? 'Marked as Audiobook (progress will auto-save)'
                  : 'Removed Audiobook status',
            ),
          ),
        );
      case 'favorite':
        song.isFavorite = !song.isFavorite;
        await IsarService.instance.updateSong(song);
        ref.invalidate(favoritesProvider);
        ref.invalidate(songsProvider);
      case 'edit':
        _showEditDialog(context, ref);
      case 'info':
        _showInfoDialog(context);
    }
  }

  void _showEditDialog(BuildContext context, WidgetRef ref) {
    final titleCtrl = TextEditingController(text: song.title);
    final artistCtrl = TextEditingController(text: song.artist);
    final albumCtrl = TextEditingController(text: song.albumTitle);
    final genreCtrl = TextEditingController(text: song.genre);

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Edit Song Tags'),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: titleCtrl,
                decoration: const InputDecoration(labelText: 'Title'),
              ),
              TextField(
                controller: artistCtrl,
                decoration: const InputDecoration(labelText: 'Artist'),
              ),
              TextField(
                controller: albumCtrl,
                decoration: const InputDecoration(labelText: 'Album'),
              ),
              TextField(
                controller: genreCtrl,
                decoration: const InputDecoration(labelText: 'Genre'),
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () async {
              await TagWriter.instance.updateSongMetadata(
                song: song,
                newTitle: titleCtrl.text.trim(),
                newArtist: artistCtrl.text.trim(),
                newAlbum: albumCtrl.text.trim(),
                newGenre: genreCtrl.text.trim(),
              );
              ref.invalidate(songsProvider);
              ref.invalidate(albumsProvider);
              ref.invalidate(artistsProvider);
              if (ctx.mounted) Navigator.pop(ctx);
            },
            child: const Text('Save'),
          ),
        ],
      ),
    );
  }

  void _showInfoDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Hi-Res Audio Info'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _infoRow('Format / Codec', song.codec ?? 'Unknown'),
            _infoRow('Bit Depth', song.bitDepth != null ? '${song.bitDepth}-bit' : 'Unknown'),
            _infoRow('Sample Rate', song.sampleRate != null ? '${song.sampleRate} Hz' : 'Unknown'),
            _infoRow('Bitrate', song.bitrate != null ? '${song.bitrate} kbps' : 'Unknown'),
            _infoRow('Quality Tier', song.quality.name.toUpperCase()),
            const SizedBox(height: 8),
            const Text('Path:', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 11)),
            Text(song.path, style: const TextStyle(fontSize: 10, color: Colors.grey)),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Close'),
          ),
        ],
      ),
    );
  }

  Widget _infoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 2),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
          Text(value, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
        ],
      ),
    );
  }
}
