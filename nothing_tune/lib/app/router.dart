import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../ui/library/library_screen.dart';
import '../ui/player/now_playing_screen.dart';
import '../ui/equalizer/eq_screen.dart';
import '../ui/streaming/url_paste_screen.dart';
import '../ui/streaming/download_queue_screen.dart';
import '../ui/streaming/radio_screen.dart';
import '../ui/settings/settings_screen.dart';

final GoRouter appRouter = GoRouter(
  initialLocation: '/library',
  routes: [
    ShellRoute(
      builder: (context, state, child) => _AppShell(child: child),
      routes: [
        GoRoute(
          path: '/library',
          builder: (context, state) => const LibraryScreen(),
        ),
        GoRoute(
          path: '/radio',
          builder: (context, state) => const RadioScreen(),
        ),
        GoRoute(
          path: '/settings',
          builder: (context, state) => const SettingsScreen(),
        ),
      ],
    ),
    GoRoute(
      path: '/player',
      builder: (context, state) => const NowPlayingScreen(),
    ),
    GoRoute(
      path: '/eq',
      builder: (context, state) => const EqScreen(),
    ),
    GoRoute(
      path: '/url-paste',
      builder: (context, state) => const UrlPasteScreen(),
    ),
    GoRoute(
      path: '/downloads',
      builder: (context, state) => const DownloadQueueScreen(),
    ),
  ],
);

class _AppShell extends StatefulWidget {
  final Widget child;
  const _AppShell({required this.child});

  @override
  State<_AppShell> createState() => _AppShellState();
}

class _AppShellState extends State<_AppShell> {
  int _selectedIndex = 0;

  static const _destinations = [
    ('/library', Icons.library_music_outlined, Icons.library_music, 'Library'),
    ('/radio', Icons.radio_outlined, Icons.radio, 'Radio'),
    ('/settings', Icons.settings_outlined, Icons.settings, 'Settings'),
  ];

  void _onTap(int index) {
    setState(() => _selectedIndex = index);
    context.go(_destinations[index].$1);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: widget.child,
      bottomNavigationBar: NavigationBar(
        selectedIndex: _selectedIndex,
        onDestinationSelected: _onTap,
        backgroundColor:
            Theme.of(context).bottomNavigationBarTheme.backgroundColor,
        destinations: _destinations
            .map(
              (d) => NavigationDestination(
                icon: Icon(d.$2),
                selectedIcon: Icon(d.$3),
                label: d.$4,
              ),
            )
            .toList(),
      ),
    );
  }
}
