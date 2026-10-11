import 'package:flutter/services.dart';

/// EQ preset band gain values in millibels for a 10-band EQ.
/// Bands (typical Android): 60Hz, 170Hz, 310Hz, 600Hz, 1kHz,
///                           3kHz, 6kHz, 12kHz, 14kHz, 16kHz
class EqPresets {
  EqPresets._();

  static const Map<String, List<int>> presets = {
    'Flat':        [0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
    'Bass Boost':  [600, 500, 400, 200, 0, 0, 0, 0, 0, 0],
    'Rock':        [400, 300, -200, -300, 200, 400, 400, 400, 300, 200],
    'Pop':         [-100, 200, 400, 400, 200, -100, -200, -200, -100, -100],
    'Classical':   [400, 300, -200, -200, -100, 0, 200, 300, 300, 400],
    'Hip-Hop':     [500, 400, 100, 400, -100, 100, 100, 100, 300, 300],
    'Jazz':        [300, 200, 100, 200, -200, -100, 100, 200, 300, 300],
    'Electronic':  [400, 300, 100, -100, -200, 200, 100, 100, 300, 400],
    'Acoustic':    [300, 200, 200, 100, -100, -100, 100, 200, 200, 300],
    'Vocal':       [-200, -100, 0, 300, 400, 300, 100, -100, -100, -200],
    'Custom':      [0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
  };

  static List<String> get names => presets.keys.toList();
}
