import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:just_audio/just_audio.dart';

import '../../providers/audio_provider.dart';
import '../../equalizer/eq_service.dart';
import '../../utils/format_utils.dart';
import '../widgets/album_art.dart';
import '../widgets/waveform_bars.dart';
import '../widgets/volume_knob.dart';
import '../equalizer/inline_eq_strip.dart';
import '../../app/theme/dot_matrix_painter.dart';
import '../../data/database/isar_service.dart';

class NowPlayingScreen extends ConsumerStatefulWidget {
  const NowPlayingScreen({super.key});

  @override
  ConsumerState<NowPlayingScreen> createState() => _NowPlayingScreenState();
}

class _NowPlayingScreenState extends ConsumerState<NowPlayingScreen>
    with TickerProviderStateMixin {
  bool _showEqBars = false;
  KnobControlMode _knobMode = KnobControlMode.volume;
  int _selectedEqBand = 0; // Default bass band
  WaveformStyle _visualizerStyle = WaveformStyle.dotMatrix;

  void _cycleVisualizerStyle() {
    setState(() {
      switch (_visualizerStyle) {
        case WaveformStyle.dotMatrix:
          _visualizerStyle = WaveformStyle.bars;
        case WaveformStyle.bars:
          _visualizerStyle = WaveformStyle.oneUiWave;
        case WaveformStyle.oneUiWave:
          _visualizerStyle = WaveformStyle.dotMatrix;
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final playerAsync = ref.watch(playerStateProvider);
    final eq = ref.watch(eqProvider);
    final cs = Theme.of(context).colorScheme;

    return playerAsync.when(
      data: (ps) {
        final song = ps.currentSong;

        return Scaffold(
          backgroundColor: Colors.black,
          appBar: AppBar(
            backgroundColor: Colors.transparent,
            leading: IconButton(
              icon: const Icon(Icons.keyboard_arrow_down),
              onPressed: () => context.pop(),
            ),
            title: Text(
              _knobMode == KnobControlMode.equalizer
                  ? 'EQ: ${_bandName(_selectedEqBand, eq)}'
                  : 'NOW PLAYING',
              style: const TextStyle(fontSize: 13, letterSpacing: 2),
            ),
            centerTitle: true,
            actions: [
              IconButton(
                icon: Icon(
                  _visualizerStyle == WaveformStyle.dotMatrix
                      ? Icons.grain
                      : _visualizerStyle == WaveformStyle.bars
                          ? Icons.bar_chart
                          : Icons.waves,
                ),
                tooltip: 'Visualizer Style (${_visualizerStyle.name})',
                onPressed: _cycleVisualizerStyle,
              ),
              IconButton(
                icon: const Icon(Icons.equalizer),
                onPressed: () => context.push('/eq'),
                tooltip: 'Full Equalizer',
              ),
              IconButton(
                icon: const Icon(Icons.more_vert),
                onPressed: () => _showOptions(context),
              ),
            ],
          ),
          body: LayoutBuilder(
            builder: (context, constraints) {
              final isWide = constraints.maxWidth >= 640;
              final isCompact = constraints.maxHeight < 560;

              if (isWide) {
                return _buildWideLayout(ps, song, cs, eq);
              } else if (isCompact) {
                return _buildCompactLayout(ps, song, cs, eq);
              } else {
                return _buildPortraitLayout(ps, song, cs, eq);
              }
            },
          ),
        );
      },
      loading: () => const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      ),
      error: (e, _) => Scaffold(body: Center(child: Text('Error: $e'))),
    );
  }

  // ── Wide Screen (Tablet / DeX / Landscape / Foldable) ──────────────────────
  Widget _buildWideLayout(
    PlayerState ps,
    dynamic song,
    ColorScheme cs,
    EqState eq,
  ) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
      child: Row(
        children: [
          // Left side: Art + Info + Visualizer
          Expanded(
            flex: 5,
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Expanded(
                  child: Center(
                    child: AspectRatio(
                      aspectRatio: 1,
                      child: ClipRRect(
                        borderRadius: BorderRadius.circular(16),
                        child: DotMatrixOverlay(
                          opacity: 0.18,
                          child: AlbumArtWidget(
                            artPath: song?.embeddedArtPath,
                            networkUrl: song?.onlineArtUrl,
                            size: double.infinity,
                          ),
                        ),
                      ),
                    ),
                  ),
                ),
                const SizedBox(height: 12),
                _buildTrackInfo(song, cs),
                const SizedBox(height: 12),
                SizedBox(
                  height: 34,
                  child: WaveformBars(
                    isPlaying: ps.isPlaying,
                    style: _visualizerStyle,
                    onTap: _cycleVisualizerStyle,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(width: 32),
          // Right side: Controls + Seek + EQ & Volume
          Expanded(
            flex: 5,
            child: SingleChildScrollView(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  _buildSeekBar(ps, cs),
                  const SizedBox(height: 8),
                  _buildPlaybackControls(ps, cs),
                  const SizedBox(height: 16),
                  if (_showEqBars) ...[
                    InlineEqStrip(
                      selectedBandIndex: _selectedEqBand,
                      onBandSelected: (idx) {
                        setState(() {
                          _selectedEqBand = idx;
                          _knobMode = KnobControlMode.equalizer;
                        });
                      },
                    ),
                    const SizedBox(height: 12),
                  ],
                  _buildKnobSection(eq, cs),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  // ── Portrait Standard Layout ──────────────────────────────────────────────
  Widget _buildPortraitLayout(
    PlayerState ps,
    dynamic song,
    ColorScheme cs,
    EqState eq,
  ) {
    return SingleChildScrollView(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
      child: Column(
        children: [
          // Art
          ConstrainedBox(
            constraints: const BoxConstraints(maxHeight: 280, maxWidth: 280),
            child: AspectRatio(
              aspectRatio: 1,
              child: ClipRRect(
                borderRadius: BorderRadius.circular(18),
                child: DotMatrixOverlay(
                  opacity: 0.20,
                  child: AlbumArtWidget(
                    artPath: song?.embeddedArtPath,
                    networkUrl: song?.onlineArtUrl,
                    size: double.infinity,
                  ),
                ),
              ),
            ),
          ),
          const SizedBox(height: 18),
          _buildTrackInfo(song, cs),
          const SizedBox(height: 12),
          SizedBox(
            height: 38,
            child: WaveformBars(
              isPlaying: ps.isPlaying,
              style: _visualizerStyle,
              onTap: _cycleVisualizerStyle,
            ),
          ),
          const SizedBox(height: 10),
          _buildSeekBar(ps, cs),
          const SizedBox(height: 10),
          _buildPlaybackControls(ps, cs),
          const SizedBox(height: 16),
          if (_showEqBars) ...[
            InlineEqStrip(
              selectedBandIndex: _selectedEqBand,
              onBandSelected: (idx) {
                setState(() {
                  _selectedEqBand = idx;
                  _knobMode = KnobControlMode.equalizer;
                });
              },
            ),
            const SizedBox(height: 12),
          ],
          _buildKnobSection(eq, cs),
          const SizedBox(height: 16),
        ],
      ),
    );
  }

  // ── Compact / Freeform Pop-up Layout ──────────────────────────────────────
  Widget _buildCompactLayout(
    PlayerState ps,
    dynamic song,
    ColorScheme cs,
    EqState eq,
  ) {
    return SingleChildScrollView(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Row(
            children: [
              ClipRRect(
                borderRadius: BorderRadius.circular(10),
                child: AlbumArtWidget(
                  artPath: song?.embeddedArtPath,
                  networkUrl: song?.onlineArtUrl,
                  size: 72,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(child: _buildTrackInfo(song, cs)),
            ],
          ),
          const SizedBox(height: 8),
          SizedBox(
            height: 24,
            child: WaveformBars(
              isPlaying: ps.isPlaying,
              barCount: 20,
              style: _visualizerStyle,
              onTap: _cycleVisualizerStyle,
            ),
          ),
          _buildSeekBar(ps, cs),
          _buildPlaybackControls(ps, cs),
          const SizedBox(height: 8),
          if (_showEqBars) ...[
            InlineEqStrip(
              selectedBandIndex: _selectedEqBand,
              onBandSelected: (idx) {
                setState(() {
                  _selectedEqBand = idx;
                  _knobMode = KnobControlMode.equalizer;
                });
              },
            ),
            const SizedBox(height: 8),
          ],
          _buildKnobSection(eq, cs),
        ],
      ),
    );
  }

  // ── Sub-widgets ───────────────────────────────────────────────────────────

  Widget _buildTrackInfo(dynamic song, ColorScheme cs) {
    return Row(
      children: [
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                song?.title ?? 'Nothing Playing',
                style: const TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                  letterSpacing: 0.5,
                ),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
              const SizedBox(height: 2),
              Text(
                song?.artist ?? '—',
                style: TextStyle(
                  fontSize: 13,
                  color: cs.onSurface.withOpacity(0.6),
                ),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ),
        ),
        IconButton(
          icon: Icon(
            song?.isFavorite == true ? Icons.favorite : Icons.favorite_border,
            color: song?.isFavorite == true ? Colors.red : null,
          ),
          onPressed: song == null
              ? null
              : () async {
                  song.isFavorite = !song.isFavorite;
                  await IsarService.instance.updateSong(song);
                  setState(() {});
                },
        ),
      ],
    );
  }

  Widget _buildSeekBar(PlayerState ps, ColorScheme cs) {
    return Column(
      children: [
        SliderTheme(
          data: SliderTheme.of(context).copyWith(
            trackHeight: 2,
            thumbShape: const RoundSliderThumbShape(enabledThumbRadius: 6),
            overlayShape: const RoundSliderOverlayShape(overlayRadius: 14),
          ),
          child: Slider(
            value: ps.duration.inMilliseconds > 0
                ? (ps.position.inMilliseconds / ps.duration.inMilliseconds)
                    .clamp(0.0, 1.0)
                : 0.0,
            onChanged: (v) {
              final target = Duration(
                milliseconds: (v * ps.duration.inMilliseconds).toInt(),
              );
              ref.read(audioPlayerServiceProvider).seekTo(target);
            },
          ),
        ),
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 10),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                FormatUtils.duration(ps.position),
                style: const TextStyle(fontSize: 10),
              ),
              Text(
                FormatUtils.duration(ps.duration),
                style: const TextStyle(fontSize: 10),
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildPlaybackControls(PlayerState ps, ColorScheme cs) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
      children: [
        IconButton(
          icon: Icon(
            Icons.shuffle,
            color: ps.shuffleEnabled ? cs.primary : cs.onSurface.withOpacity(0.35),
          ),
          onPressed: () => ref.read(audioPlayerServiceProvider).toggleShuffle(),
        ),
        IconButton(
          icon: const Icon(Icons.skip_previous, size: 34),
          onPressed: () => ref.read(audioPlayerServiceProvider).skipToPrevious(),
        ),
        GestureDetector(
          onTap: () {
            final svc = ref.read(audioPlayerServiceProvider);
            ps.isPlaying ? svc.pause() : svc.play();
          },
          child: Container(
            width: 60,
            height: 60,
            decoration: BoxDecoration(
              color: cs.primary,
              shape: BoxShape.circle,
              border: Border.all(color: cs.onSurface.withOpacity(0.2)),
            ),
            child: Icon(
              ps.isPlaying ? Icons.pause : Icons.play_arrow,
              color: cs.onPrimary,
              size: 30,
            ),
          ),
        ),
        IconButton(
          icon: const Icon(Icons.skip_next, size: 34),
          onPressed: () => ref.read(audioPlayerServiceProvider).skipToNext(),
        ),
        IconButton(
          icon: Icon(
            ps.loopMode == LoopMode.one ? Icons.repeat_one : Icons.repeat,
            color: ps.loopMode == LoopMode.off ? cs.onSurface.withOpacity(0.35) : cs.primary,
          ),
          onPressed: () => ref.read(audioPlayerServiceProvider).cycleLoopMode(),
        ),
      ],
    );
  }

  Widget _buildKnobSection(EqState eq, ColorScheme cs) {
    final isEqMode = _knobMode == KnobControlMode.equalizer;

    double knobValue;
    String knobLabel;

    if (!isEqMode) {
      knobValue = ref.read(audioPlayerServiceProvider).volume;
      knobLabel = 'VOL';
    } else {
      knobValue = (_selectedEqBand < eq.bands.length ? eq.bands[_selectedEqBand] : 0).toDouble();
      knobLabel = _bandName(_selectedEqBand, eq);
    }

    return Column(
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            // Mode toggle button (VOL / EQ)
            ActionChip(
              avatar: Icon(
                isEqMode ? Icons.tune : Icons.volume_up,
                size: 14,
                color: isEqMode ? Colors.amberAccent : cs.primary,
              ),
              label: Text(
                isEqMode ? 'EQ MODE' : 'VOL MODE',
                style: TextStyle(
                  fontSize: 10,
                  fontWeight: FontWeight.bold,
                  color: isEqMode ? Colors.amberAccent : cs.primary,
                ),
              ),
              backgroundColor: isEqMode ? Colors.amber.withOpacity(0.12) : cs.surface,
              side: BorderSide(color: isEqMode ? Colors.amber : cs.outline),
              onPressed: () {
                setState(() {
                  _knobMode = isEqMode ? KnobControlMode.volume : KnobControlMode.equalizer;
                });
              },
            ),
            const SizedBox(width: 12),
            // Expand / collapse EQ bars toggle button
            ActionChip(
              avatar: Icon(
                _showEqBars ? Icons.expand_less : Icons.expand_more,
                size: 14,
              ),
              label: Text(
                _showEqBars ? 'HIDE BARS' : 'EQ BARS',
                style: const TextStyle(fontSize: 10),
              ),
              onPressed: () => setState(() => _showEqBars = !_showEqBars),
            ),
          ],
        ),
        const SizedBox(height: 10),
        VolumeKnob(
          value: knobValue,
          mode: _knobMode,
          label: knobLabel,
          size: 80,
          onChanged: (newVal) {
            if (!isEqMode) {
              ref.read(audioPlayerServiceProvider).setVolume(newVal);
            } else {
              ref.read(eqProvider.notifier).setBandLevel(_selectedEqBand, newVal.toInt());
            }
          },
          onModeToggle: () {
            setState(() {
              _knobMode = isEqMode ? KnobControlMode.volume : KnobControlMode.equalizer;
            });
          },
          onExpandEq: () => setState(() => _showEqBars = !_showEqBars),
        ),
      ],
    );
  }

  String _bandName(int bandIdx, EqState eq) {
    if (bandIdx < eq.bandFreqs.length) {
      final hz = eq.bandFreqs[bandIdx];
      return hz >= 1000 ? '${hz ~/ 1000}kHz' : '${hz}Hz';
    }
    return 'Band $bandIdx';
  }

  void _showOptions(BuildContext context) {
    showModalBottomSheet(
      context: context,
      builder: (_) => Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          ListTile(
            leading: const Icon(Icons.queue_music),
            title: const Text('View queue'),
            onTap: () {
              Navigator.pop(context);
              _showQueue(context);
            },
          ),
          ListTile(
            leading: const Icon(Icons.speed),
            title: const Text('Playback speed'),
            onTap: () {
              Navigator.pop(context);
              _showSpeedDialog(context);
            },
          ),
          ListTile(
            leading: const Icon(Icons.timer_outlined),
            title: const Text('Sleep timer'),
            onTap: () {
              Navigator.pop(context);
              _showSleepTimer(context);
            },
          ),
        ],
      ),
    );
  }

  void _showQueue(BuildContext context) {
    final ps = ref.read(playerStateProvider).value;
    if (ps == null) return;
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      builder: (_) => DraggableScrollableSheet(
        expand: false,
        initialChildSize: 0.6,
        builder: (_, controller) => ReorderableListView.builder(
          scrollController: controller,
          itemCount: ps.queue.length,
          onReorder: (o, n) =>
              ref.read(audioPlayerServiceProvider).reorderQueue(o, n),
          itemBuilder: (ctx, i) {
            final s = ps.queue[i];
            return ListTile(
              key: ValueKey(s.id),
              leading: i == ps.queueIndex
                  ? const Icon(Icons.play_arrow)
                  : const Icon(Icons.drag_handle),
              title: Text(s.title, maxLines: 1, overflow: TextOverflow.ellipsis),
              subtitle: Text(s.artist),
              trailing: IconButton(
                icon: const Icon(Icons.close),
                onPressed: () =>
                    ref.read(audioPlayerServiceProvider).removeFromQueue(i),
              ),
            );
          },
        ),
      ),
    );
  }

  void _showSpeedDialog(BuildContext context) {
    double speed = 1.0;
    showDialog(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setState) => AlertDialog(
          title: const Text('Playback speed'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text('${speed.toStringAsFixed(2)}x'),
              Slider(
                value: speed,
                min: 0.25,
                max: 3.0,
                divisions: 55,
                onChanged: (v) {
                  setState(() => speed = v);
                  ref.read(audioPlayerServiceProvider).setSpeed(v);
                },
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () {
                ref.read(audioPlayerServiceProvider).setSpeed(1.0);
                Navigator.pop(ctx);
              },
              child: const Text('Reset'),
            ),
            TextButton(
              onPressed: () => Navigator.pop(ctx),
              child: const Text('Done'),
            ),
          ],
        ),
      ),
    );
  }

  void _showSleepTimer(BuildContext context) {
    final options = [15, 30, 45, 60, 90];
    showModalBottomSheet(
      context: context,
      builder: (_) => Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Padding(
            padding: EdgeInsets.all(16),
            child: Text('Sleep timer', style: TextStyle(fontWeight: FontWeight.bold)),
          ),
          ...options.map(
            (min) => ListTile(
              title: Text('$min minutes'),
              onTap: () {
                Future.delayed(Duration(minutes: min), () {
                  ref.read(audioPlayerServiceProvider).pause();
                });
                Navigator.pop(context);
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text('Sleep timer set for $min min')),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
