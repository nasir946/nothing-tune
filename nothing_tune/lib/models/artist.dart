import 'package:isar/isar.dart';

part 'artist.g.dart';

@collection
class Artist {
  Id id = Isar.autoIncrement;

  @Index(unique: true)
  late String name;

  int songCount = 0;
  int albumCount = 0;

  String? artPath;
  String? onlineArtUrl;

  String? mbid;
}
