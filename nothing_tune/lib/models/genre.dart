import 'package:isar/isar.dart';

part 'genre.g.dart';

@collection
class Genre {
  Id id = Isar.autoIncrement;

  @Index(unique: true)
  late String name;

  int songCount = 0;
}
