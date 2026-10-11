import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'eq_presets.dart';

const _channel = MethodChannel('com.nothing.tune/equalizer');

class EqState {
  final bool enabled;
  final List<int> bands;       // millibels per band
  final int bassBoost;         // 0–1000
  final int virtualizer;       // 0–1000
  final bool loudnessEnabled;
  final String preset;
  final int bandCount;
  final List<int> bandFreqs;   // Hz labels

  const EqState({
    required this.enabled,
    required this.bands,
    required this.bassBoost,
    required this.virtualizer,
    required this.loudnessEnabled,
    required this.preset,
    required this.bandCount,
    required this.bandFreqs,
  });

  EqState copyWith({
    bool? enabled,
    List<int>? bands,
    int? bassBoost,
    int? virtualizer,
    bool? loudnessEnabled,
    String? preset,
    int? bandCount,
    List<int>? bandFreqs,
  }) =>
      EqState(
        enabled: enabled ?? this.enabled,
        bands: bands ?? this.bands,
        bassBoost: bassBoost ?? this.bassBoost,
        virtualizer: virtualizer ?? this.virtualizer,
        loudnessEnabled: loudnessEnabled ?? this.loudnessEnabled,
        preset: preset ?? this.preset,
        bandCount: bandCount ?? this.bandCount,
        bandFreqs: bandFreqs ?? this.bandFreqs,
      );
}

class EqNotifier extends Notifier<EqState> {
  @override
  EqState build() {
    _init();
    return const EqState(
      enabled: false,
      bands: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
      bassBoost: 0,
      virtualizer: 0,
      loudnessEnabled: false,
      preset: 'Flat',
      bandCount: 10,
      bandFreqs: [60, 170, 310, 600, 1000, 3000, 6000, 12000, 14000, 16000],
    );
  }

  Future<void> _init() async {
    try {
      final result = await _channel.invokeMethod('init') as Map?;
      if (result != null) {
        state = state.copyWith(
          bandCount: result['bandCount'] as int?,
          bandFreqs: (result['bandFreqs'] as List?)?.cast<int>(),
        );
      }
    } catch (_) {
      // EQ not supported on this device — no-op
    }
  }

  Future<void> setEnabled(bool v) async {
    state = state.copyWith(enabled: v);
    try {
      await _channel.invokeMethod('setEnabled', {'enabled': v});
    } catch (_) {}
  }

  Future<void> setBandLevel(int band, int milliDb) async {
    final bands = List<int>.from(state.bands);
    if (band < bands.length) bands[band] = milliDb;
    state = state.copyWith(bands: bands, preset: 'Custom');
    try {
      await _channel.invokeMethod('setBand', {'band': band, 'level': milliDb});
    } catch (_) {}
  }

  Future<void> applyPreset(String name) async {
    final levels = EqPresets.presets[name] ?? List.filled(state.bandCount, 0);
    state = state.copyWith(bands: levels, preset: name);
    try {
      for (int i = 0; i < levels.length; i++) {
        await _channel.invokeMethod('setBand', {'band': i, 'level': levels[i]});
      }
    } catch (_) {}
  }

  Future<void> setBassBoost(int strength) async {
    state = state.copyWith(bassBoost: strength);
    try {
      await _channel.invokeMethod('setBassBoost', {'strength': strength});
    } catch (_) {}
  }

  Future<void> setVirtualizer(int strength) async {
    state = state.copyWith(virtualizer: strength);
    try {
      await _channel.invokeMethod('setVirtualizer', {'strength': strength});
    } catch (_) {}
  }

  Future<void> setLoudness(bool v) async {
    state = state.copyWith(loudnessEnabled: v);
    try {
      await _channel.invokeMethod('setLoudness', {'enabled': v});
    } catch (_) {}
  }
}

final eqProvider = NotifierProvider<EqNotifier, EqState>(EqNotifier.new);
