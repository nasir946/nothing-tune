import 'package:isar/isar.dart';

part 'album.g.dart';

@collection
class Album {
  Id id = Isar.autoIncrement;

  @Index(unique: true)
  late String title;

  late String artist;
  int? year;
  int songCount = 0;

  String? artPath;
  String? onlineArtUrl;

  // MusicBrainz ID for metadata fetch
  String? mbid;
}
