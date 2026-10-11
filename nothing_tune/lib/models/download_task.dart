import 'package:isar/isar.dart';

part 'download_task.g.dart';

enum DownloadStatus { queued, downloading, completed, failed, cancelled }

@collection
class DownloadTask {
  Id id = Isar.autoIncrement;

  late String url;
  late String title;
  String? artist;
  String? thumbnailUrl;

  /// Chosen quality label: 'best' | '320k' | '128k' | 'flac'
  late String quality;

  @Enumerated(EnumType.name)
  DownloadStatus status = DownloadStatus.queued;

  double progress = 0.0; // 0.0 – 1.0
  String? outputPath;
  String? errorMessage;

  int createdMs = 0;
  int completedMs = 0;
}
