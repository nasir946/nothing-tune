import 'package:dio/dio.dart';
import '../../config/app_config.dart';

class MusicBrainzRelease {
  final String mbid;
  final String title;
  final String? artist;
  final String? date;
  final String? coverArtUrl;

  MusicBrainzRelease({
    required this.mbid,
    required this.title,
    this.artist,
    this.date,
    this.coverArtUrl,
  });
}

class MusicBrainzClient {
  MusicBrainzClient._();
  static final MusicBrainzClient instance = MusicBrainzClient._();

  final Dio _dio = Dio(
    BaseOptions(
      baseUrl: 'https://musicbrainz.org/ws/2/',
      headers: {
        'User-Agent': AppConfig.musicBrainzUserAgent,
        'Accept': 'application/json',
      },
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 10),
    ),
  );

  /// Search for release metadata and find cover art
  Future<MusicBrainzRelease?> searchRelease({
    required String albumTitle,
    String? artistName,
  }) async {
    try {
      String query = 'release:"$albumTitle"';
      if (artistName != null && artistName.isNotEmpty) {
        query += ' AND artist:"$artistName"';
      }

      final response = await _dio.get(
        'release',
        queryParameters: {
          'query': query,
          'fmt': 'json',
          'limit': 5,
        },
      );

      final releases = response.data['releases'] as List<dynamic>?;
      if (releases == null || releases.isEmpty) return null;

      final first = releases.first as Map<String, dynamic>;
      final mbid = first['id'] as String;
      final title = (first['title'] as String?) ?? albumTitle;
      final date = first['date'] as String?;
      
      // Cover Art Archive URL directly by release mbid
      final coverUrl = 'https://coverartarchive.org/release/$mbid/front';

      return MusicBrainzRelease(
        mbid: mbid,
        title: title,
        artist: artistName,
        date: date,
        coverArtUrl: coverUrl,
      );
    } catch (_) {
      return null;
    }
  }

  /// Verify if cover art exists on Cover Art Archive for given MBID
  Future<String?> getCoverArtUrl(String mbid) async {
    final directUrl = 'https://coverartarchive.org/release/$mbid/front';
    try {
      final res = await Dio().head(
        directUrl,
        options: Options(
          followRedirects: true,
          validateStatus: (status) => status != null && status < 400,
        ),
      );
      if (res.statusCode == 200 || res.statusCode == 307) {
        return directUrl;
      }
    } catch (_) {}
    return null;
  }
}
