import 'dart:ui';
import 'package:flutter/material.dart';

/// A card-like container with a frosted-glass appearance.
/// Used in the Glassmorphism theme.
class GlassCard extends StatelessWidget {
  final Widget child;
  final double blur;
  final double opacity;
  final double borderRadius;
  final EdgeInsetsGeometry padding;
  final Color? tint;

  const GlassCard({
    super.key,
    required this.child,
    this.blur = 20,
    this.opacity = 0.12,
    this.borderRadius = 20,
    this.padding = const EdgeInsets.all(16),
    this.tint,
  });

  @override
  Widget build(BuildContext context) {
    final color = (tint ?? Colors.white).withOpacity(opacity);
    return ClipRRect(
      borderRadius: BorderRadius.circular(borderRadius),
      child: BackdropFilter(
        filter: ImageFilter.blur(sigmaX: blur, sigmaY: blur),
        child: Container(
          padding: padding,
          decoration: BoxDecoration(
            color: color,
            borderRadius: BorderRadius.circular(borderRadius),
            border: Border.all(
              color: Colors.white.withOpacity(0.18),
              width: 1,
            ),
          ),
          child: child,
        ),
      ),
    );
  }
}
