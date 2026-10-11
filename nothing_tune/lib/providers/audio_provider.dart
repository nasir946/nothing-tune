import 'package:audio_service/audio_service.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../audio/audio_handler.dart';
import '../audio/audio_player_service.dart';
import '../models/song.dart';

// Singleton instances — initialised in main.dart before runApp
late final AudioPlayerService audioPlayerService;
late final NothingAudioHandler audioHandler;

final audioPlayerServiceProvider = Provider<AudioPlayerService>(
  (ref) => audioPlayerService,
);

final playerStateProvider = StreamProvider((ref) {
  return ref.watch(audioPlayerServiceProvider).state;
});
