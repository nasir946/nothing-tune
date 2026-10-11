import 'package:isar/isar.dart';

part 'radio_station.g.dart';

@collection
class RadioStation {
  Id id = Isar.autoIncrement;

  late String name;
  late String url;   // m3u / HLS / direct stream URL
  String? genre;
  String? logoUrl;
  bool isFavorite = false;
}
