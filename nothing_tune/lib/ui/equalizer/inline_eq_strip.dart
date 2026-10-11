import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../equalizer/eq_service.dart';

/// Interactive 5-band EQ strip with visual vertical bars that can be dragged
/// directly or selected for knob control.
class InlineEqStrip extends ConsumerWidget {
  final int selectedBandIndex;
  final ValueChanged<int>? onBandSelected;

  const InlineEqStrip({
    super.key,
    this.selectedBandIndex = 0,
    this.onBandSelected,
  });

  static const displayBands = [0, 2, 4, 6, 9]; // Bass -> Low-Mid -> Mid -> High-Mid -> Treble

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final eq = ref.watch(eqProvider);
    final cs = Theme.of(context).colorScheme;

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      decoration: BoxDecoration(
        color: cs.surface.withOpacity(0.85),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: cs.outline.withOpacity(0.4)),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'EQUALIZER BARS (PRESET: ${eq.preset.toUpperCase()})',
                style: TextStyle(
                  fontSize: 10,
                  letterSpacing: 1.5,
                  fontWeight: FontWeight.bold,
                  color: cs.primary,
                ),
              ),
              Switch(
                value: eq.enabled,
                onChanged: (v) => ref.read(eqProvider.notifier).setEnabled(v),
                materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
              ),
            ],
          ),
          const SizedBox(height: 6),
          SizedBox(
            height: 95,
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                for (final bandIdx in displayBands)
                  if (bandIdx < eq.bands.length)
                    _InteractiveBandBar(
                      freqLabel: bandIdx < eq.bandFreqs.length
                          ? _freqLabel(eq.bandFreqs[bandIdx])
                          : '$bandIdx',
                      levelMilliDb: eq.bands[bandIdx],
                      isSelected: selectedBandIndex == bandIdx,
                      enabled: eq.enabled,
                      onTap: () => onBandSelected?.call(bandIdx),
                      onChanged: (newLevel) {
                        ref.read(eqProvider.notifier).setBandLevel(bandIdx, newLevel);
                        onBandSelected?.call(bandIdx);
                      },
                    ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  static String _freqLabel(int hz) => hz >= 1000 ? '${hz ~/ 1000}k' : '$hz';
}

class _InteractiveBandBar extends StatelessWidget {
  final String freqLabel;
  final int levelMilliDb;
  final bool isSelected;
  final bool enabled;
  final VoidCallback onTap;
  final ValueChanged<int> onChanged;

  const _InteractiveBandBar({
    required this.freqLabel,
    required this.levelMilliDb,
    required this.isSelected,
    required this.enabled,
    required this.onTap,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final activeColor = isSelected ? Colors.amberAccent : cs.primary;
    final db = (levelMilliDb / 100).round();
    final dbText = db > 0 ? '+$db' : '$db';

    // Fraction 0..1 where 0.5 is 0dB
    final fraction = ((levelMilliDb + 1500) / 3000).clamp(0.0, 1.0);

    return GestureDetector(
      onTap: onTap,
      onVerticalDragUpdate: enabled
          ? (d) {
              // Drag up increases level, drag down decreases
              final delta = -d.primaryDelta! / 60.0;
              final newFrac = (fraction + delta).clamp(0.0, 1.0);
              final newLevel = ((newFrac * 3000) - 1500).round();
              onChanged(newLevel);
            }
          : null,
      child: Container(
        width: 44,
        padding: const EdgeInsets.symmetric(vertical: 4),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(8),
          color: isSelected ? activeColor.withOpacity(0.12) : Colors.transparent,
          border: isSelected
              ? Border.all(color: activeColor.withOpacity(0.5), width: 1)
              : null,
        ),
        child: Column(
          children: [
            Text(
              dbText,
              style: TextStyle(
                fontSize: 9,
                fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                color: enabled ? activeColor : cs.onSurface.withOpacity(0.3),
              ),
            ),
            const SizedBox(height: 4),
            Expanded(
              child: Center(
                child: Container(
                  width: 8,
                  decoration: BoxDecoration(
                    color: cs.outline.withOpacity(0.25),
                    borderRadius: BorderRadius.circular(4),
                  ),
                  alignment: Alignment.bottomCenter,
                  child: FractionallySizedBox(
                    heightFactor: fraction,
                    child: Container(
                      decoration: BoxDecoration(
                        color: enabled ? activeColor : cs.onSurface.withOpacity(0.2),
                        borderRadius: BorderRadius.circular(4),
                      ),
                    ),
                  ),
                ),
              ),
            ),
            const SizedBox(height: 4),
            Text(
              freqLabel,
              style: TextStyle(
                fontSize: 9,
                fontWeight: FontWeight.w600,
                color: isSelected ? activeColor : cs.onSurface.withOpacity(0.6),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
