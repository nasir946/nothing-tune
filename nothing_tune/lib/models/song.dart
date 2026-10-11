import 'package:isar/isar.dart';

part 'song.g.dart';

// Quality tier enum
enum AudioQuality { hires, lossless, standard }

@collection
class Song {
  Id id = Isar.autoIncrement;

  @Index(unique: true)
  late String path; // absolute file path

  late String title;
  late String artist;
  late String albumTitle;
  late String genre;

  int? trackNumber;
  int? discNumber;
  int? year;
  int? durationMs;

  // Audio properties
  int? bitrate;       // kbps
  int? sampleRate;    // Hz  (e.g. 192000)
  int? bitDepth;      // bits (e.g. 24)
  String? codec;      // FLAC, MP3, AAC …

  // Computed quality tier — stored for fast filtering
  @Enumerated(EnumType.name)
  AudioQuality quality = AudioQuality.standard;

  // Art
  String? embeddedArtPath; // extracted to cache dir on scan
  String? onlineArtUrl;

  // Library state
  int playCount = 0;
  int? lastPlayedMs; // epoch millis
  int addedMs = 0;   // epoch millis
  bool isFavorite = false;
  bool isAudiobook = false;
  int audiobookPositionMs = 0; // resume position for audiobooks

  // Relations (stored as IDs for Isar)
  int albumId = 0;
  int artistId = 0;
}
