import 'package:isar/isar.dart';

part 'playlist.g.dart';

@collection
class Playlist {
  Id id = Isar.autoIncrement;

  @Index(unique: true)
  late String name;

  // Ordered list of song IDs
  List<int> songIds = [];

  int createdMs = 0;
  int modifiedMs = 0;

  // Smart playlist type — null means user-created
  String? smartType; // 'recent' | 'mostPlayed' | 'unplayed' | 'favorites'
}
