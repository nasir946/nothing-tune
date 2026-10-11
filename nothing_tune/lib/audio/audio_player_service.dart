import 'dart:async';
import 'package:audio_service/audio_service.dart';
import 'package:just_audio/just_audio.dart';
import 'package:rxdart/rxdart.dart';

import '../models/song.dart';
import '../data/database/isar_service.dart';
import '../data/lastfm/lastfm_scrobbler.dart';
import '../config/app_config.dart';

/// Combined state exposed to the UI.
class PlayerState {
  final Song? currentSong;
  final bool isPlaying;
  final Duration position;
  final Duration duration;
  final bool shuffleEnabled;
  final LoopMode loopMode;
  final List<Song> queue;
  final int queueIndex;

  const PlayerState({
    this.currentSong,
    required this.isPlaying,
    required this.position,
    required this.duration,
    required this.shuffleEnabled,
    required this.loopMode,
    required this.queue,
    required this.queueIndex,
  });

  PlayerState copyWith({
    Song? currentSong,
    bool? isPlaying,
    Duration? position,
    Duration? duration,
    bool? shuffleEnabled,
    LoopMode? loopMode,
    List<Song>? queue,
    int? queueIndex,
  }) =>
      PlayerState(
        currentSong: currentSong ?? this.currentSong,
        isPlaying: isPlaying ?? this.isPlaying,
        position: position ?? this.position,
        duration: duration ?? this.duration,
        shuffleEnabled: shuffleEnabled ?? this.shuffleEnabled,
        loopMode: loopMode ?? this.loopMode,
        queue: queue ?? this.queue,
        queueIndex: queueIndex ?? this.queueIndex,
      );
}

/// Wraps just_audio's AudioPlayer and exposes a single reactive [state] stream.
class AudioPlayerService {
  final AudioPlayer _player = AudioPlayer();

  final BehaviorSubject<PlayerState> _stateSubject =
      BehaviorSubject<PlayerState>.seeded(
    const PlayerState(
      isPlaying: false,
      position: Duration.zero,
      duration: Duration.zero,
      shuffleEnabled: false,
      loopMode: LoopMode.off,
      queue: [],
      queueIndex: 0,
    ),
  );

  ValueStream<PlayerState> get state => _stateSubject.stream;
  PlayerState get currentState => _stateSubject.value;

  List<Song> _queue = [];
  int _queueIndex = 0;
  bool _shuffleEnabled = false;
  LoopMode _loopMode = LoopMode.off;

  bool _scrobbledCurrentTrack = false;
  Timer? _audiobookSaveTimer;

  AudioPlayerService() {
    // Position updates
    _player.positionStream.listen((pos) {
      _emit(position: pos);
      _checkScrobbleThreshold(pos);
    });

    // Duration updates
    _player.durationStream.listen((dur) {
      _emit(duration: dur ?? Duration.zero);
    });

    // Playing state
    _player.playingStream.listen((playing) {
      _emit(isPlaying: playing);
      if (playing) {
        _startAudiobookTimer();
      } else {
        _stopAudiobookTimer();
        _saveAudiobookProgress();
      }
    });

    // Auto-advance
    _player.processingStateStream.listen((state) async {
      if (state == ProcessingState.completed) {
        await _handleCompletion();
      }
    });
  }

  void _emit({
    Song? currentSong,
    bool? isPlaying,
    Duration? position,
    Duration? duration,
    bool? shuffleEnabled,
    LoopMode? loopMode,
    List<Song>? queue,
    int? queueIndex,
  }) {
    _stateSubject.add(
      _stateSubject.value.copyWith(
        currentSong: currentSong,
        isPlaying: isPlaying,
        position: position,
        duration: duration,
        shuffleEnabled: shuffleEnabled,
        loopMode: loopMode,
        queue: queue,
        queueIndex: queueIndex,
      ),
    );
  }

  // ── Scrobbling & Bookmarks ────────────────────────────────────────────────

  void _checkScrobbleThreshold(Duration currentPos) {
    final song = currentState.currentSong;
    if (song == null || _scrobbledCurrentTrack) return;

    final dur = currentState.duration;
    if (dur.inSeconds < 30) return; // Ignore very short tracks

    final halfDur = Duration(milliseconds: (dur.inMilliseconds * AppConfig.scrobbleThreshold).toInt());
    final threshold = halfDur < AppConfig.scrobbleMaxDuration ? halfDur : AppConfig.scrobbleMaxDuration;

    if (currentPos >= threshold) {
      _scrobbledCurrentTrack = true;
      LastFmScrobbler.instance.scrobble(song);
    }
  }

  void _startAudiobookTimer() {
    _audiobookSaveTimer?.cancel();
    _audiobookSaveTimer = Timer.periodic(const Duration(seconds: 5), (_) {
      _saveAudiobookProgress();
    });
  }

  void _stopAudiobookTimer() {
    _audiobookSaveTimer?.cancel();
    _audiobookSaveTimer = null;
  }

  Future<void> _saveAudiobookProgress() async {
    final s = currentState.currentSong;
    if (s != null && s.isAudiobook) {
      s.audiobookPositionMs = currentState.position.inMilliseconds;
      await IsarService.instance.updateSong(s);
    }
  }

  // ── Playback ───────────────────────────────────────────────────────────────

  Future<void> playSong(Song song, {List<Song>? queue, int index = 0}) async {
    _saveAudiobookProgress();

    _queue = queue ?? [song];
    _queueIndex = index;
    _scrobbledCurrentTrack = false;

    // Handle audiobook resume
    final startPos = song.isAudiobook && song.audiobookPositionMs > 0
        ? Duration(milliseconds: song.audiobookPositionMs)
        : Duration.zero;

    final isRemote = song.path.startsWith('http://') ||
        song.path.startsWith('https://') ||
        song.path.startsWith('rtsp://');

    final audioUri = isRemote ? Uri.parse(song.path) : Uri.file(song.path);

    await _player.setAudioSource(
      AudioSource.uri(audioUri),
      initialPosition: startPos,
    );
    await _player.play();

    _emit(currentSong: song, queue: _queue, queueIndex: _queueIndex);
    await IsarService.instance.incrementPlayCount(song.id);

    // Update Last.fm now playing status
    LastFmScrobbler.instance.updateNowPlaying(song);
  }

  Future<void> playQueue(List<Song> queue, {int startIndex = 0}) async {
    if (queue.isEmpty) return;
    await playSong(queue[startIndex], queue: queue, index: startIndex);
  }

  Future<void> play() => _player.play();

  Future<void> pause() async {
    await _player.pause();
    await _saveAudiobookProgress();
  }

  Future<void> stop() async {
    await _saveAudiobookProgress();
    await _player.stop();
    _emit(isPlaying: false, position: Duration.zero);
  }

  Future<void> seekTo(Duration position) => _player.seek(position);

  Future<void> skipToNext() async {
    if (_queue.isEmpty) return;
    await _saveAudiobookProgress();
    if (_shuffleEnabled) {
      _queueIndex = (DateTime.now().millisecondsSinceEpoch % _queue.length).toInt();
    } else {
      _queueIndex = (_queueIndex + 1) % _queue.length;
    }
    await playSong(_queue[_queueIndex], queue: _queue, index: _queueIndex);
  }

  Future<void> skipToPrevious() async {
    if (_queue.isEmpty) return;
    await _saveAudiobookProgress();
    if (currentState.position.inSeconds > 3) {
      await seekTo(Duration.zero);
      return;
    }
    _queueIndex = (_queueIndex - 1 + _queue.length) % _queue.length;
    await playSong(_queue[_queueIndex], queue: _queue, index: _queueIndex);
  }

  // ── Queue management ───────────────────────────────────────────────────────

  void addToQueue(Song song) {
    _queue = [..._queue, song];
    _emit(queue: _queue);
  }

  void removeFromQueue(int index) {
    final newQueue = List<Song>.from(_queue)..removeAt(index);
    if (_queueIndex >= newQueue.length) _queueIndex = newQueue.length - 1;
    _queue = newQueue;
    _emit(queue: _queue, queueIndex: _queueIndex);
  }

  void reorderQueue(int oldIndex, int newIndex) {
    final newQueue = List<Song>.from(_queue);
    final item = newQueue.removeAt(oldIndex);
    newQueue.insert(newIndex, item);
    _queue = newQueue;
    _emit(queue: _queue);
  }

  // ── Shuffle & Repeat ───────────────────────────────────────────────────────

  Future<void> toggleShuffle() async {
    _shuffleEnabled = !_shuffleEnabled;
    _emit(shuffleEnabled: _shuffleEnabled);
  }

  Future<void> cycleLoopMode() async {
    switch (_loopMode) {
      case LoopMode.off:
        _loopMode = LoopMode.all;
      case LoopMode.all:
        _loopMode = LoopMode.one;
      case LoopMode.one:
        _loopMode = LoopMode.off;
    }
    await _player.setLoopMode(_loopMode);
    _emit(loopMode: _loopMode);
  }

  // ── Speed & Pitch ──────────────────────────────────────────────────────────

  Future<void> setSpeed(double speed) => _player.setSpeed(speed);
  Future<void> setPitch(double pitch) => _player.setPitch(pitch);

  // ── Volume ─────────────────────────────────────────────────────────────────

  Future<void> setVolume(double volume) => _player.setVolume(volume.clamp(0.0, 1.0));
  double get volume => _player.volume;

  /// Stream a radio URL (m3u / HLS / direct).
  Future<void> playRadio(String url, String stationName) async {
    await _saveAudiobookProgress();
    final song = Song()
      ..path = url
      ..title = stationName
      ..artist = 'Radio'
      ..albumTitle = 'Radio'
      ..genre = 'Radio'
      ..addedMs = DateTime.now().millisecondsSinceEpoch;

    _queue = [song];
    _queueIndex = 0;
    _scrobbledCurrentTrack = true; // Don't scrobble radio streams

    await _player.setAudioSource(AudioSource.uri(Uri.parse(url)));
    await _player.play();
    _emit(currentSong: song, queue: _queue, queueIndex: 0);
  }

  // ── Internal ───────────────────────────────────────────────────────────────

  Future<void> _handleCompletion() async {
    if (_loopMode == LoopMode.one) {
      await seekTo(Duration.zero);
      await _player.play();
      return;
    }
    final isLast = _queueIndex == _queue.length - 1;
    if (isLast && _loopMode == LoopMode.off) {
      _emit(isPlaying: false);
      return;
    }
    await skipToNext();
  }

  Future<void> dispose() async {
    _stopAudiobookTimer();
    await _stateSubject.close();
    await _player.dispose();
  }
}
