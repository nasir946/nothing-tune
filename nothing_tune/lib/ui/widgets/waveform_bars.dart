import 'dart:math' as math;
import 'package:flutter/material.dart';

enum WaveformStyle { dotMatrix, bars, oneUiWave }

/// Animated music visualizer supporting:
/// 1. Nothing OS Dot Matrix (stacked discrete dots)
/// 2. Clean equalizer bars
/// 3. One UI smooth fluid waveform
class WaveformBars extends StatefulWidget {
  final bool isPlaying;
  final int barCount;
  final Color? color;
  final WaveformStyle style;
  final VoidCallback? onTap;

  const WaveformBars({
    super.key,
    required this.isPlaying,
    this.barCount = 28,
    this.color,
    this.style = WaveformStyle.dotMatrix,
    this.onTap,
  });

  @override
  State<WaveformBars> createState() => _WaveformBarsState();
}

class _WaveformBarsState extends State<WaveformBars>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1400),
    );
    if (widget.isPlaying) _controller.repeat();
  }

  @override
  void didUpdateWidget(WaveformBars old) {
    super.didUpdateWidget(old);
    if (widget.isPlaying && !_controller.isAnimating) {
      _controller.repeat();
    } else if (!widget.isPlaying && _controller.isAnimating) {
      _controller.stop();
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final color = widget.color ?? Theme.of(context).colorScheme.primary;

    return GestureDetector(
      onTap: widget.onTap,
      child: AnimatedBuilder(
        animation: _controller,
        builder: (_, __) => CustomPaint(
          painter: _VisualizerPainter(
            progress: _controller.value,
            isPlaying: widget.isPlaying,
            barCount: widget.barCount,
            color: color,
            style: widget.style,
          ),
          size: Size.infinite,
        ),
      ),
    );
  }
}

class _VisualizerPainter extends CustomPainter {
  final double progress;
  final bool isPlaying;
  final int barCount;
  final Color color;
  final WaveformStyle style;

  _VisualizerPainter({
    required this.progress,
    required this.isPlaying,
    required this.barCount,
    required this.color,
    required this.style,
  });

  @override
  void paint(Canvas canvas, Size size) {
    switch (style) {
      case WaveformStyle.dotMatrix:
        _paintDotMatrix(canvas, size);
      case WaveformStyle.bars:
        _paintBars(canvas, size);
      case WaveformStyle.oneUiWave:
        _paintOneUiWave(canvas, size);
    }
  }

  // ── 1. Nothing OS Dot Matrix (stacked glowing dots) ────────────────────────
  void _paintDotMatrix(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..style = PaintingStyle.fill;

    final dimPaint = Paint()
      ..color = color.withOpacity(0.12)
      ..style = PaintingStyle.fill;

    const maxDotsPerBar = 7;
    final dotDiameter = math.min(size.height / (maxDotsPerBar * 1.5), size.width / (barCount * 1.5));
    final dotRadius = dotDiameter / 2;
    final stepX = size.width / barCount;
    final stepY = size.height / maxDotsPerBar;

    for (int col = 0; col < barCount; col++) {
      final x = col * stepX + stepX / 2;

      int activeDots = 1;
      if (isPlaying) {
        final phase = (col / barCount) * math.pi * 2;
        final wave = math.sin(progress * math.pi * 2 + phase);
        final wave2 = math.cos(progress * math.pi * 3 + phase * 1.4);
        final fraction = (0.2 + 0.8 * ((wave + wave2 + 2) / 4)).clamp(0.0, 1.0);
        activeDots = (fraction * maxDotsPerBar).round().clamp(1, maxDotsPerBar);
      }

      for (int row = 0; row < maxDotsPerBar; row++) {
        final y = size.height - (row * stepY + stepY / 2);
        final isLit = row < activeDots;
        canvas.drawCircle(Offset(x, y), dotRadius, isLit ? paint : dimPaint);
      }
    }
  }

  // ── 2. Rounded Equalizer Bars ─────────────────────────────────────────────
  void _paintBars(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..style = PaintingStyle.fill;

    final barW = (size.width / barCount) * 0.55;
    final gap = (size.width / barCount) * 0.45;

    for (int i = 0; i < barCount; i++) {
      final x = i * (barW + gap);

      double fraction = 0.12;
      if (isPlaying) {
        final phase = (i / barCount) * math.pi * 2;
        final wave = math.sin(progress * math.pi * 2 + phase);
        final wave2 = math.sin(progress * math.pi * 4 + phase * 1.7);
        fraction = (0.15 + 0.75 * ((wave + wave2 + 2) / 4)).clamp(0.1, 1.0);
      }

      final barH = size.height * fraction;
      final top = size.height - barH;

      final rect = RRect.fromRectAndRadius(
        Rect.fromLTWH(x, top, barW, barH),
        const Radius.circular(3),
      );
      canvas.drawRRect(rect, paint);
    }
  }

  // ── 3. One UI Smooth Fluid Waveform ───────────────────────────────────────
  void _paintOneUiWave(Canvas canvas, Size size) {
    final strokePaint = Paint()
      ..color = color
      ..style = PaintingStyle.stroke
      ..strokeWidth = 3.0
      ..strokeCap = StrokeCap.round;

    final fillPaint = Paint()
      ..shader = LinearGradient(
        begin: Alignment.topCenter,
        end: Alignment.bottomCenter,
        colors: [
          color.withOpacity(0.35),
          color.withOpacity(0.02),
        ],
      ).createShader(Rect.fromLTWH(0, 0, size.width, size.height))
      ..style = PaintingStyle.fill;

    final path = Path();
    final fillPath = Path();

    final midY = size.height / 2;
    path.moveTo(0, midY);
    fillPath.moveTo(0, size.height);
    fillPath.lineTo(0, midY);

    const segments = 40;
    final dx = size.width / segments;

    for (int i = 1; i <= segments; i++) {
      final x = i * dx;
      double y = midY;

      if (isPlaying) {
        final phase = (i / segments) * math.pi * 3;
        final wave1 = math.sin(progress * math.pi * 2 + phase);
        final wave2 = math.cos(progress * math.pi * 4 + phase * 1.5);
        final amp = (size.height * 0.4) * ((wave1 + wave2 * 0.5) / 1.5);
        y = midY + amp;
      }

      path.lineTo(x, y);
      fillPath.lineTo(x, y);
    }

    fillPath.lineTo(size.width, size.height);
    fillPath.close();

    canvas.drawPath(fillPath, fillPaint);
    canvas.drawPath(path, strokePaint);
  }

  @override
  bool shouldRepaint(_VisualizerPainter old) =>
      old.progress != progress ||
      old.isPlaying != isPlaying ||
      old.style != style ||
      old.color != color;
}
