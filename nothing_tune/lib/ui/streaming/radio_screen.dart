import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../data/database/isar_service.dart';
import '../../models/radio_station.dart';
import '../../providers/audio_provider.dart';
import '../widgets/mini_player.dart';

class RadioScreen extends ConsumerStatefulWidget {
  const RadioScreen({super.key});

  @override
  ConsumerState<RadioScreen> createState() => _RadioScreenState();
}

class _RadioScreenState extends ConsumerState<RadioScreen> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('RADIO')),
      body: Column(
        children: [
          Expanded(
            child: FutureBuilder<List<RadioStation>>(
              future: IsarService.instance.getAllStations(),
              builder: (context, snapshot) {
                if (!snapshot.hasData) {
                  return const Center(child: CircularProgressIndicator());
                }
                final stations = snapshot.data!;
                if (stations.isEmpty) {
                  return Center(
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        const Icon(Icons.radio, size: 64),
                        const SizedBox(height: 16),
                        const Text('No radio stations added.'),
                        const SizedBox(height: 8),
                        FilledButton.icon(
                          onPressed: () => _addStation(context),
                          icon: const Icon(Icons.add),
                          label: const Text('Add station'),
                        ),
                      ],
                    ),
                  );
                }
                return ListView.builder(
                  itemCount: stations.length,
                  itemBuilder: (ctx, i) {
                    final s = stations[i];
                    return ListTile(
                      leading: s.logoUrl != null
                          ? ClipRRect(
                              borderRadius: BorderRadius.circular(6),
                              child: Image.network(
                                s.logoUrl!,
                                width: 44,
                                height: 44,
                                fit: BoxFit.cover,
                                errorBuilder: (_, __, ___) =>
                                    const Icon(Icons.radio),
                              ),
                            )
                          : const Icon(Icons.radio),
                      title: Text(s.name),
                      subtitle: Text(s.genre ?? s.url,
                          maxLines: 1, overflow: TextOverflow.ellipsis),
                      trailing: IconButton(
                        icon: const Icon(Icons.delete_outline),
                        onPressed: () async {
                          await IsarService.instance.deleteStation(s.id);
                          setState(() {});
                        },
                      ),
                      onTap: () async {
                        await ref
                            .read(audioPlayerServiceProvider)
                            .playRadio(s.url, s.name);
                        ctx.push('/player');
                      },
                    );
                  },
                );
              },
            ),
          ),
          const MiniPlayer(),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => _addStation(context),
        child: const Icon(Icons.add),
      ),
    );
  }

  void _addStation(BuildContext context) {
    final nameCtrl = TextEditingController();
    final urlCtrl = TextEditingController();
    final genreCtrl = TextEditingController();

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Add Radio Station'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: nameCtrl,
              decoration: const InputDecoration(hintText: 'Station name'),
            ),
            const SizedBox(height: 8),
            TextField(
              controller: urlCtrl,
              decoration:
                  const InputDecoration(hintText: 'Stream URL (m3u / HLS)'),
            ),
            const SizedBox(height: 8),
            TextField(
              controller: genreCtrl,
              decoration:
                  const InputDecoration(hintText: 'Genre (optional)'),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () async {
              if (nameCtrl.text.trim().isEmpty || urlCtrl.text.trim().isEmpty) {
                return;
              }
              final station = RadioStation()
                ..name = nameCtrl.text.trim()
                ..url = urlCtrl.text.trim()
                ..genre = genreCtrl.text.trim().isEmpty
                    ? null
                    : genreCtrl.text.trim();
              await IsarService.instance.saveStation(station);
              if (ctx.mounted) Navigator.pop(ctx);
              setState(() {});
            },
            child: const Text('Add'),
          ),
        ],
      ),
    );
  }
}
