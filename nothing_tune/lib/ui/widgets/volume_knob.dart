import 'dart:math' as math;
import 'package:flutter/material.dart';

enum KnobControlMode { volume, equalizer }

/// A multifunctional round Nothing OS-styled knob:
/// 1. Controls Volume (0% - 100%)
/// 2. Controls Equalizer gain (-15dB to +15dB) with numerical readout
/// 3. Toggle between Volume and Equalizer modes via center tap or button
class VolumeKnob extends StatefulWidget {
  final double value; // 0..1 for volume, or -1500..1500 for EQ
  final KnobControlMode mode;
  final ValueChanged<double> onChanged;
  final VoidCallback? onModeToggle;
  final VoidCallback? onExpandEq;
  final String? label;
  final double size;

  const VolumeKnob({
    super.key,
    required this.value,
    this.mode = KnobControlMode.volume,
    required this.onChanged,
    this.onModeToggle,
    this.onExpandEq,
    this.label,
    this.size = 78,
  });

  @override
  State<VolumeKnob> createState() => _VolumeKnobState();
}

class _VolumeKnobState extends State<VolumeKnob> {
  static const _minAngle = -135 * math.pi / 180;
  static const _maxAngle = 135 * math.pi / 180;

  double get _fraction {
    if (widget.mode == KnobControlMode.volume) {
      return widget.value.clamp(0.0, 1.0);
    } else {
      // -1500 .. +1500 milliB
      return ((widget.value + 1500) / 3000).clamp(0.0, 1.0);
    }
  }

  double get _angle => _minAngle + (_fraction * (_maxAngle - _minAngle));

  Offset? _lastPosition;

  void _onPanStart(DragStartDetails d) => _lastPosition = d.localPosition;

  void _onPanUpdate(DragUpdateDetails d) {
    final center = Offset(widget.size / 2, widget.size / 2);
    final prev = _lastPosition! - center;
    final curr = d.localPosition - center;

    final prevAngle = math.atan2(prev.dy, prev.dx);
    final currAngle = math.atan2(curr.dy, curr.dx);
    var delta = currAngle - prevAngle;

    if (delta > math.pi) delta -= 2 * math.pi;
    if (delta < -math.pi) delta += 2 * math.pi;

    final newFraction = (_fraction + delta / (_maxAngle - _minAngle)).clamp(0.0, 1.0);

    if (widget.mode == KnobControlMode.volume) {
      widget.onChanged(newFraction);
    } else {
      final milliDb = ((newFraction * 3000) - 1500).roundToDouble();
      widget.onChanged(milliDb);
    }

    _lastPosition = d.localPosition;
  }

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final isEq = widget.mode == KnobControlMode.equalizer;

    String displayText;
    if (!isEq) {
      displayText = '${(widget.value * 100).toInt()}%';
    } else {
      final db = (widget.value / 100).round();
      displayText = db > 0 ? '+$db' : '$db';
    }

    return GestureDetector(
      onTap: widget.onModeToggle,
      onLongPress: widget.onExpandEq,
      onPanStart: _onPanStart,
      onPanUpdate: _onPanUpdate,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          SizedBox(
            width: widget.size,
            height: widget.size,
            child: Stack(
              alignment: Alignment.center,
              children: [
                CustomPaint(
                  size: Size(widget.size, widget.size),
                  painter: _KnobPainter(
                    angle: _angle,
                    color: isEq ? Colors.amberAccent : cs.primary,
                    trackColor: cs.outline,
                    fraction: _fraction,
                  ),
                ),
                Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(
                      displayText,
                      style: TextStyle(
                        fontSize: 13,
                        fontWeight: FontWeight.bold,
                        color: cs.onSurface,
                        letterSpacing: -0.5,
                      ),
                    ),
                    Text(
                      widget.label ?? (isEq ? 'EQ' : 'VOL'),
                      style: TextStyle(
                        fontSize: 9,
                        letterSpacing: 1.0,
                        color: isEq ? Colors.amberAccent : cs.onSurface.withOpacity(0.5),
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _KnobPainter extends CustomPainter {
  final double angle;
  final double fraction;
  final Color color;
  final Color trackColor;

  _KnobPainter({
    required this.angle,
    required this.fraction,
    required this.color,
    required this.trackColor,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final radius = size.width / 2 - 4;

    // Outer track
    final trackPaint = Paint()
      ..color = trackColor.withOpacity(0.3)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 3.5
      ..strokeCap = StrokeCap.round;

    canvas.drawCircle(center, radius, trackPaint);

    // Active arc
    const startAngle = 135 * math.pi / 180;
    final sweepAngle = (angle - (-135 * math.pi / 180));
    final arcPaint = Paint()
      ..color = color
      ..style = PaintingStyle.stroke
      ..strokeWidth = 3.5
      ..strokeCap = StrokeCap.round;

    canvas.drawArc(
      Rect.fromCircle(center: center, radius: radius),
      math.pi / 2 + startAngle,
      sweepAngle,
      false,
      arcPaint,
    );

    // Indicator dot
    final dotPos = Offset(
      center.dx + radius * math.cos(math.pi / 2 + angle),
      center.dy + radius * math.sin(math.pi / 2 + angle),
    );
    final dotPaint = Paint()..color = color;
    canvas.drawCircle(dotPos, 4.5, dotPaint);

    // Dark body with Nothing OS dot texture border
    final bodyPaint = Paint()
      ..color = Colors.black
      ..style = PaintingStyle.fill;
    canvas.drawCircle(center, radius - 7, bodyPaint);

    final borderPaint = Paint()
      ..color = trackColor.withOpacity(0.6)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1;
    canvas.drawCircle(center, radius - 7, borderPaint);
  }

  @override
  bool shouldRepaint(_KnobPainter old) =>
      old.angle != angle || old.color != color || old.fraction != fraction;
}
