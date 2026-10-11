import 'dart:math' as math;
import 'package:flutter/material.dart';

/// Paints a dot-matrix grid overlay on top of album art.
/// Used in the Now Playing screen and ambient display.
class DotMatrixPainter extends CustomPainter {
  final Color dotColor;
  final double dotRadius;
  final double spacing;
  final double opacity;

  DotMatrixPainter({
    this.dotColor = Colors.black,
    this.dotRadius = 1.2,
    this.spacing = 8.0,
    this.opacity = 0.35,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = dotColor.withOpacity(opacity)
      ..style = PaintingStyle.fill;

    final cols = (size.width / spacing).ceil();
    final rows = (size.height / spacing).ceil();

    for (int r = 0; r <= rows; r++) {
      for (int c = 0; c <= cols; c++) {
        canvas.drawCircle(
          Offset(c * spacing, r * spacing),
          dotRadius,
          paint,
        );
      }
    }
  }

  @override
  bool shouldRepaint(DotMatrixPainter old) =>
      old.opacity != opacity ||
      old.dotColor != dotColor ||
      old.dotRadius != dotRadius;
}

/// Widget wrapper — place over any child to apply the dot overlay.
class DotMatrixOverlay extends StatelessWidget {
  final Widget child;
  final Color dotColor;
  final double opacity;
  final double spacing;

  const DotMatrixOverlay({
    super.key,
    required this.child,
    this.dotColor = Colors.black,
    this.opacity = 0.30,
    this.spacing = 8.0,
  });

  @override
  Widget build(BuildContext context) {
    return Stack(
      fit: StackFit.expand,
      children: [
        child,
        IgnorePointer(
          child: CustomPaint(
            painter: DotMatrixPainter(
              dotColor: dotColor,
              opacity: opacity,
              spacing: spacing,
            ),
          ),
        ),
      ],
    );
  }
}
