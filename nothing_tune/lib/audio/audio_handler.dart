import 'package:audio_service/audio_service.dart';
import 'package:just_audio/just_audio.dart';

import 'audio_player_service.dart';
import '../models/song.dart';

/// Bridges audio_service (system media controls / notification)
/// with our [AudioPlayerService].
class NothingAudioHandler extends BaseAudioHandler with QueueHandler, SeekHandler {
  final AudioPlayerService _playerService;

  NothingAudioHandler(this._playerService) {
    // Forward player state → audio_service playback state
    _playerService.state.listen((ps) {
      playbackState.add(
        PlaybackState(
          controls: [
            MediaControl.skipToPrevious,
            ps.isPlaying ? MediaControl.pause : MediaControl.play,
            MediaControl.skipToNext,
          ],
          systemActions: const {
            MediaAction.seek,
            MediaAction.seekForward,
            MediaAction.seekBackward,
          },
          androidCompactActionIndices: const [0, 1, 2],
          processingState: AudioProcessingState.ready,
          playing: ps.isPlaying,
          updatePosition: ps.position,
          bufferedPosition: ps.duration,
          speed: 1.0,
        ),
      );

      // Update media item
      final song = ps.currentSong;
      if (song != null) {
        mediaItem.add(
          MediaItem(
            id: song.path,
            title: song.title,
            artist: song.artist,
            album: song.albumTitle,
            duration: ps.duration,
            artUri: song.embeddedArtPath != null
                ? Uri.file(song.embeddedArtPath!)
                : null,
          ),
        );
      }
    });
  }

  @override
  Future<void> play() => _playerService.play();

  @override
  Future<void> pause() => _playerService.pause();

  @override
  Future<void> stop() => _playerService.stop();

  @override
  Future<void> seek(Duration position) => _playerService.seekTo(position);

  @override
  Future<void> skipToNext() => _playerService.skipToNext();

  @override
  Future<void> skipToPrevious() => _playerService.skipToPrevious();
}
