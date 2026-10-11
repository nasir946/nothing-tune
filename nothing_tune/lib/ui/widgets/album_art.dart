import 'dart:io';
import 'package:flutter/material.dart';

/// Album art widget with embedded-art file support and a dot-matrix placeholder.
class AlbumArtWidget extends StatelessWidget {
  final String? artPath;
  final String? networkUrl;
  final double size;
  final double borderRadius;

  const AlbumArtWidget({
    super.key,
    this.artPath,
    this.networkUrl,
    required this.size,
    this.borderRadius = 12,
  });

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;

    Widget art;

    if (artPath != null && File(artPath!).existsSync()) {
      art = Image.file(
        File(artPath!),
        width: size,
        height: size,
        fit: BoxFit.cover,
        errorBuilder: (_, __, ___) => _placeholder(cs),
      );
    } else if (networkUrl != null) {
      art = Image.network(
        networkUrl!,
        width: size,
        height: size,
        fit: BoxFit.cover,
        errorBuilder: (_, __, ___) => _placeholder(cs),
        loadingBuilder: (_, child, progress) =>
            progress == null ? child : _placeholder(cs),
      );
    } else {
      art = _placeholder(cs);
    }

    return ClipRRect(
      borderRadius: BorderRadius.circular(borderRadius),
      child: art,
    );
  }

  Widget _placeholder(ColorScheme cs) {
    return Container(
      width: size,
      height: size,
      color: cs.outline.withOpacity(0.3),
      child: Icon(
        Icons.music_note,
        size: size is double && size > 60 ? 48 : 24,
        color: cs.onSurface.withOpacity(0.3),
      ),
    );
  }
}
