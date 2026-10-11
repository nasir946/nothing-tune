import 'dart:io';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:path_provider/path_provider.dart';
import 'package:youtube_explode_dart/youtube_explode_dart.dart';

import '../../models/download_task.dart';
import '../../models/song.dart';
import '../../data/database/isar_service.dart';
import '../../providers/audio_provider.dart';

const _channel = MethodChannel('com.nothing.tune/ytdlp');

class YtDlpService {
  YtDlpService._();
  static final YtDlpService instance = YtDlpService._();

  final YoutubeExplode _yt = YoutubeExplode();

  bool _isYouTubeUrl(String url) {
    return url.contains('youtube.com') ||
        url.contains('youtu.be') ||
        url.contains('music.youtube.com');
  }

  /// Returns metadata info for a URL (title, uploader, thumbnail, duration, direct stream url).
  Future<Map<String, dynamic>> getInfo(String url) async {
    // 1. If YouTube link, try pure Dart youtube_explode first for speed & reliability
    if (_isYouTubeUrl(url)) {
      try {
        final video = await _yt.videos.get(url);
        final manifest = await _yt.videos.streamsClient.getManifest(video.id);
        final audioOnly = manifest.audioOnly.withHighestBitrate();

        return {
          'title': video.title,
          'uploader': video.author,
          'thumbnail': video.thumbnails.highResUrl,
          'duration': video.duration?.inSeconds ?? 0,
          'url': audioOnly.url.toString(),
          'webpage_url': url,
        };
      } catch (_) {
        // Fall back to native yt-dlp if youtube_explode fails
      }
    }

    // 2. Native yt-dlp (handles SoundCloud, Bandcamp, direct links, etc.)
    try {
      final result = await _channel.invokeMethod('getInfo', {'url': url});
      return Map<String, dynamic>.from(result as Map);
    } on PlatformException catch (e) {
      throw 'Stream resolution error: ${e.message}';
    }
  }

  /// Queue a download task.
  Future<void> startDownload(DownloadTask task) async {
    // If YouTube URL, can download directly via pure Dart
    if (_isYouTubeUrl(task.url)) {
      _downloadYouTubePureDart(task);
      return;
    }

    // Otherwise use native yt-dlp plugin
    try {
      await _channel.invokeMethod('download', {
        'id': task.id,
        'url': task.url,
        'quality': task.quality,
      });
    } catch (_) {}
  }

  Future<void> _downloadYouTubePureDart(DownloadTask task) async {
    try {
      task.status = DownloadStatus.downloading;
      await IsarService.instance.saveDownloadTask(task);

      final video = await _yt.videos.get(task.url);
      final manifest = await _yt.videos.streamsClient.getManifest(video.id);
      final audioStreamInfo = manifest.audioOnly.withHighestBitrate();

      final dir = await getExternalStorageDirectory() ??
          await getApplicationDocumentsDirectory();
      final musicDir = Directory('${dir.path}/Music/NothingTune');
      if (!musicDir.existsSync()) {
        musicDir.createSync(recursive: true);
      }

      final safeTitle = video.title.replaceAll(RegExp(r'[\\/:*?"<>|]'), '_');
      final ext = audioStreamInfo.container.name;
      final file = File('${musicDir.path}/$safeTitle.$ext');

      final stream = _yt.videos.streamsClient.get(audioStreamInfo);
      final fileStream = file.openWrite();

      int bytesWritten = 0;
      final totalBytes = audioStreamInfo.size.totalBytes;

      await for (final chunk in stream) {
        bytesWritten += chunk.length;
        fileStream.add(chunk);
        if (totalBytes > 0) {
          task.progress = (bytesWritten / totalBytes).clamp(0.0, 1.0);
          await IsarService.instance.saveDownloadTask(task);
        }
      }

      await fileStream.flush();
      await fileStream.close();

      task.status = DownloadStatus.completed;
      task.outputPath = file.path;
      task.progress = 1.0;
      task.completedMs = DateTime.now().millisecondsSinceEpoch;
      await IsarService.instance.saveDownloadTask(task);
    } catch (e) {
      task.status = DownloadStatus.failed;
      task.errorMessage = e.toString();
      await IsarService.instance.saveDownloadTask(task);
    }
  }

  /// Resolve stream URL and play directly without downloading.
  Future<void> streamUrl(String url, WidgetRef ref) async {
    try {
      final info = await getInfo(url);
      final streamUrl = info['url'] as String? ?? url;
      final song = Song()
        ..path = streamUrl
        ..title = (info['title'] as String?) ?? 'Stream'
        ..artist = (info['uploader'] as String?) ?? 'Online'
        ..albumTitle = 'Online Streams'
        ..genre = 'Stream'
        ..onlineArtUrl = info['thumbnail'] as String?
        ..addedMs = DateTime.now().millisecondsSinceEpoch;

      await ref.read(audioPlayerServiceProvider).playSong(song);
    } catch (e) {
      throw 'Stream error: $e';
    }
  }

  void dispose() {
    _yt.close();
  }
}
