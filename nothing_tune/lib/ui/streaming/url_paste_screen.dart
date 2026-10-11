import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../data/streaming/ytdlp_service.dart';
import '../../models/download_task.dart';
import '../../data/database/isar_service.dart';

class UrlPasteScreen extends ConsumerStatefulWidget {
  const UrlPasteScreen({super.key});

  @override
  ConsumerState<UrlPasteScreen> createState() => _UrlPasteScreenState();
}

class _UrlPasteScreenState extends ConsumerState<UrlPasteScreen> {
  final _controller = TextEditingController();
  String _selectedQuality = 'best';
  bool _isLoading = false;
  String? _error;
  Map<String, dynamic>? _info;

  final _qualities = [
    ('best', 'Best Quality'),
    ('flac', 'Lossless FLAC'),
    ('320k', 'MP3 320kbps'),
    ('128k', 'MP3 128kbps'),
  ];

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Future<void> _fetchInfo() async {
    final url = _controller.text.trim();
    if (url.isEmpty) return;
    setState(() {
      _isLoading = true;
      _error = null;
      _info = null;
    });
    try {
      final info = await YtDlpService.instance.getInfo(url);
      setState(() {
        _info = info;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString();
        _isLoading = false;
      });
    }
  }

  Future<void> _startDownload() async {
    if (_info == null) return;
    final task = DownloadTask()
      ..url = _controller.text.trim()
      ..title = (_info!['title'] as String?) ?? 'Unknown'
      ..artist = _info!['uploader'] as String?
      ..thumbnailUrl = _info!['thumbnail'] as String?
      ..quality = _selectedQuality
      ..status = DownloadStatus.queued
      ..createdMs = DateTime.now().millisecondsSinceEpoch;

    await IsarService.instance.saveDownloadTask(task);
    YtDlpService.instance.startDownload(task);

    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Download queued')),
      );
      context.pop();
    }
  }

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    return Scaffold(
      appBar: AppBar(title: const Text('STREAM / DOWNLOAD')),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          const Text(
            'Paste a YouTube, SoundCloud or direct audio link:',
            style: TextStyle(fontSize: 13),
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: _controller,
                  decoration: InputDecoration(
                    hintText: 'https://...',
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(10),
                    ),
                    suffixIcon: IconButton(
                      icon: const Icon(Icons.paste),
                      onPressed: () async {
                        final data = await Clipboard.getData('text/plain');
                        if (data?.text != null) {
                          _controller.text = data!.text!;
                        }
                      },
                    ),
                  ),
                  onSubmitted: (_) => _fetchInfo(),
                ),
              ),
              const SizedBox(width: 8),
              FilledButton(
                onPressed: _isLoading ? null : _fetchInfo,
                child: _isLoading
                    ? const SizedBox(
                        width: 20,
                        height: 20,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                    : const Icon(Icons.search),
              ),
            ],
          ),

          if (_error != null) ...[
            const SizedBox(height: 12),
            Text('Error: $_error',
                style: const TextStyle(color: Colors.redAccent)),
          ],

          if (_info != null) ...[
            const SizedBox(height: 20),
            const Divider(),
            const SizedBox(height: 12),

            // Thumbnail
            if (_info!['thumbnail'] != null)
              ClipRRect(
                borderRadius: BorderRadius.circular(10),
                child: Image.network(
                  _info!['thumbnail'] as String,
                  height: 160,
                  fit: BoxFit.cover,
                  errorBuilder: (_, __, ___) => const SizedBox.shrink(),
                ),
              ),

            const SizedBox(height: 12),
            Text(
              (_info!['title'] as String?) ?? 'Unknown',
              style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
            ),
            if (_info!['uploader'] != null)
              Text(
                _info!['uploader'] as String,
                style: TextStyle(color: cs.onSurface.withOpacity(0.6)),
              ),

            const SizedBox(height: 20),
            const Text('QUALITY', style: TextStyle(fontSize: 11, letterSpacing: 2)),
            const SizedBox(height: 8),
            Wrap(
              spacing: 8,
              children: _qualities.map((q) {
                final selected = _selectedQuality == q.$1;
                return ChoiceChip(
                  label: Text(q.$2),
                  selected: selected,
                  onSelected: (_) => setState(() => _selectedQuality = q.$1),
                );
              }).toList(),
            ),

            const SizedBox(height: 24),
            FilledButton.icon(
              onPressed: _startDownload,
              icon: const Icon(Icons.download),
              label: const Text('Download'),
            ),
            const SizedBox(height: 8),
            OutlinedButton.icon(
              onPressed: () async {
                // Stream directly without downloading
                await YtDlpService.instance.streamUrl(
                  _controller.text.trim(),
                  ref,
                );
                if (mounted) context.push('/player');
              },
              icon: const Icon(Icons.play_circle_outline),
              label: const Text('Play now (stream)'),
            ),
          ],
        ],
      ),
    );
  }
}
