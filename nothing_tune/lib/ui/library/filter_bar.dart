import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../models/song.dart';
import '../../providers/library_provider.dart';

class FilterBar extends ConsumerWidget {
  const FilterBar({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final current = ref.watch(qualityFilterProvider);
    final cs = Theme.of(context).colorScheme;

    final filters = [
      (null, 'All'),
      (AudioQuality.hires, 'Hi-Res'),
      (AudioQuality.lossless, 'Lossless'),
      (AudioQuality.standard, 'Standard'),
    ];

    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      child: Row(
        children: filters.map((f) {
          final selected = current == f.$1;
          return Padding(
            padding: const EdgeInsets.only(right: 8),
            child: FilterChip(
              label: Text(f.$2),
              selected: selected,
              onSelected: (_) =>
                  ref.read(qualityFilterProvider.notifier).state = f.$1,
              backgroundColor: cs.surface,
              selectedColor: cs.primary,
              labelStyle: TextStyle(
                color: selected ? cs.onPrimary : cs.onSurface,
                fontSize: 12,
              ),
              side: BorderSide(color: cs.outline),
              showCheckmark: false,
            ),
          );
        }).toList(),
      ),
    );
  }
}
