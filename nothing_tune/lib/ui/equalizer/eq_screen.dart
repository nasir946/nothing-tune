import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../equalizer/eq_service.dart';

/// Full 10-band equalizer screen.
class EqScreen extends ConsumerWidget {
  const EqScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final eq = ref.watch(eqProvider);
    final cs = Theme.of(context).colorScheme;

    return Scaffold(
      appBar: AppBar(title: const Text('EQUALIZER')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // ── Enable toggle ───────────────────────────────────────────────
          SwitchListTile(
            title: const Text('Equalizer'),
            value: eq.enabled,
            onChanged: (v) => ref.read(eqProvider.notifier).setEnabled(v),
          ),

          const Divider(),

          // ── Preset selector ─────────────────────────────────────────────
          const Text('PRESET', style: TextStyle(fontSize: 11, letterSpacing: 2)),
          const SizedBox(height: 8),
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Row(
              children: [
                for (final name in _presetNames(eq))
                  Padding(
                    padding: const EdgeInsets.only(right: 8),
                    child: ChoiceChip(
                      label: Text(name),
                      selected: eq.preset == name,
                      onSelected: (_) =>
                          ref.read(eqProvider.notifier).applyPreset(name),
                    ),
                  ),
              ],
            ),
          ),

          const SizedBox(height: 24),

          // ── Band sliders ────────────────────────────────────────────────
          const Text('BANDS', style: TextStyle(fontSize: 11, letterSpacing: 2)),
          const SizedBox(height: 8),
          SizedBox(
            height: 220,
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: List.generate(eq.bands.length, (i) {
                final freq = i < eq.bandFreqs.length ? eq.bandFreqs[i] : null;
                return _BandSlider(
                  label: freq != null ? _freqLabel(freq) : '$i',
                  value: eq.bands[i].toDouble(),
                  min: -1500,
                  max: 1500,
                  onChanged: eq.enabled
                      ? (v) => ref
                          .read(eqProvider.notifier)
                          .setBandLevel(i, v.toInt())
                      : null,
                );
              }),
            ),
          ),

          const SizedBox(height: 24),
          const Divider(),

          // ── Bass Boost ──────────────────────────────────────────────────
          const Text('BASS BOOST', style: TextStyle(fontSize: 11, letterSpacing: 2)),
          Slider(
            value: eq.bassBoost.toDouble(),
            min: 0,
            max: 1000,
            divisions: 100,
            label: '${eq.bassBoost}',
            onChanged: eq.enabled
                ? (v) =>
                    ref.read(eqProvider.notifier).setBassBoost(v.toInt())
                : null,
          ),

          // ── Virtualizer ─────────────────────────────────────────────────
          const Text('VIRTUALIZER', style: TextStyle(fontSize: 11, letterSpacing: 2)),
          Slider(
            value: eq.virtualizer.toDouble(),
            min: 0,
            max: 1000,
            divisions: 100,
            label: '${eq.virtualizer}',
            onChanged: eq.enabled
                ? (v) =>
                    ref.read(eqProvider.notifier).setVirtualizer(v.toInt())
                : null,
          ),

          // ── Loudness Enhancer ────────────────────────────────────────────
          SwitchListTile(
            title: const Text('Loudness Enhancer'),
            value: eq.loudnessEnabled,
            onChanged: eq.enabled
                ? (v) => ref.read(eqProvider.notifier).setLoudness(v)
                : null,
          ),
        ],
      ),
    );
  }

  List<String> _presetNames(EqState eq) {
    final names = [
      'Flat', 'Bass Boost', 'Rock', 'Pop', 'Classical',
      'Hip-Hop', 'Jazz', 'Electronic', 'Acoustic', 'Vocal',
    ];
    if (eq.preset == 'Custom') names.add('Custom');
    return names;
  }

  String _freqLabel(int hz) {
    if (hz >= 1000) return '${hz ~/ 1000}k';
    return '$hz';
  }
}

class _BandSlider extends StatelessWidget {
  final String label;
  final double value;
  final double min;
  final double max;
  final ValueChanged<double>? onChanged;

  const _BandSlider({
    required this.label,
    required this.value,
    required this.min,
    required this.max,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    return Column(
      children: [
        Text(
          '${(value / 100).toStringAsFixed(0)}',
          style: const TextStyle(fontSize: 9),
        ),
        Expanded(
          child: RotatedBox(
            quarterTurns: 3,
            child: Slider(
              value: value,
              min: min,
              max: max,
              onChanged: onChanged,
            ),
          ),
        ),
        Text(label, style: const TextStyle(fontSize: 9)),
      ],
    );
  }
}
