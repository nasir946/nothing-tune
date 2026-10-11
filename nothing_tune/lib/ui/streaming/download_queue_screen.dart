import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../data/database/isar_service.dart';
import '../../models/download_task.dart';

class DownloadQueueScreen extends ConsumerWidget {
  const DownloadQueueScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(title: const Text('DOWNLOADS')),
      body: FutureBuilder<List<DownloadTask>>(
        future: IsarService.instance.getAllDownloadTasks(),
        builder: (context, snapshot) {
          if (!snapshot.hasData) {
            return const Center(child: CircularProgressIndicator());
          }
          final tasks = snapshot.data!;
          if (tasks.isEmpty) {
            return const Center(child: Text('No downloads yet.'));
          }
          return ListView.builder(
            itemCount: tasks.length,
            itemBuilder: (ctx, i) {
              final t = tasks[i];
              return ListTile(
                leading: t.thumbnailUrl != null
                    ? ClipRRect(
                        borderRadius: BorderRadius.circular(6),
                        child: Image.network(
                          t.thumbnailUrl!,
                          width: 44,
                          height: 44,
                          fit: BoxFit.cover,
                          errorBuilder: (_, __, ___) =>
                              const Icon(Icons.music_note),
                        ),
                      )
                    : const Icon(Icons.music_note),
                title: Text(t.title, maxLines: 1, overflow: TextOverflow.ellipsis),
                subtitle: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('${t.quality} · ${_statusLabel(t.status)}'),
                    if (t.status == DownloadStatus.downloading)
                      LinearProgressIndicator(value: t.progress),
                    if (t.status == DownloadStatus.failed && t.errorMessage != null)
                      Text(
                        t.errorMessage!,
                        style: const TextStyle(color: Colors.redAccent, fontSize: 11),
                      ),
                  ],
                ),
                trailing: IconButton(
                  icon: const Icon(Icons.delete_outline),
                  onPressed: () async {
                    await IsarService.instance.deleteDownloadTask(t.id);
                  },
                ),
              );
            },
          );
        },
      ),
    );
  }

  String _statusLabel(DownloadStatus s) {
    switch (s) {
      case DownloadStatus.queued: return 'Queued';
      case DownloadStatus.downloading: return 'Downloading';
      case DownloadStatus.completed: return 'Done';
      case DownloadStatus.failed: return 'Failed';
      case DownloadStatus.cancelled: return 'Cancelled';
    }
  }
}
