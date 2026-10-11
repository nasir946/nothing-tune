import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:flex_color_picker/flex_color_picker.dart';

import '../../config/app_config.dart';
import '../../providers/theme_provider.dart';
import '../../providers/app_name_provider.dart';
import '../../data/lastfm/lastfm_auth.dart';
import '../../data/metadata/musicbrainz_client.dart';
import '../../data/database/isar_service.dart';

class SettingsScreen extends ConsumerWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final themeMode = ref.watch(themeProvider);
    final accentArgb = ref.watch(accentColorProvider);
    final currentAppName = ref.watch(appNameProvider);
    final accent = Color(accentArgb);
    final cs = Theme.of(context).colorScheme;

    return Scaffold(
      appBar: AppBar(title: const Text('SETTINGS')),
      body: ListView(
        children: [
          // ── App Customization ─────────────────────────────────────────────
          const _SectionHeader('APP CUSTOMIZATION'),

          ListTile(
            title: const Text('App Name'),
            subtitle: Text(currentAppName),
            leading: const Icon(Icons.edit_outlined),
            trailing: const Icon(Icons.arrow_forward_ios, size: 14),
            onTap: () => _showAppNameDialog(context, ref, currentAppName),
          ),

          ListTile(
            title: const Text('Theme'),
            subtitle: Text(themeMode.name.toUpperCase()),
            leading: const Icon(Icons.palette_outlined),
            onTap: () => _showThemePicker(context, ref, themeMode),
          ),

          if (themeMode != AppThemeMode.dotMono)
            ListTile(
              title: const Text('Accent color'),
              leading: const Icon(Icons.color_lens_outlined),
              trailing: Container(
                width: 28,
                height: 28,
                decoration: BoxDecoration(
                  color: accent,
                  shape: BoxShape.circle,
                  border: Border.all(color: cs.outline),
                ),
              ),
              onTap: () => _showColorPicker(context, ref, accent),
            ),

          const Divider(),

          // ── Audio ────────────────────────────────────────────────────────
          const _SectionHeader('AUDIO'),

          ListTile(
            title: const Text('Equalizer'),
            subtitle: const Text('10-band EQ, bass boost & virtualizer'),
            leading: const Icon(Icons.equalizer),
            onTap: () => context.push('/eq'),
          ),

          const ListTile(
            title: Text('Hi-Res Audio Passthrough'),
            subtitle: Text('24-bit / 192kHz exclusive mode enabled'),
            leading: Icon(Icons.graphic_eq),
          ),

          const Divider(),

          // ── Metadata & Online ─────────────────────────────────────────────
          const _SectionHeader('ONLINE & METADATA'),

          _LastFmTile(),

          ListTile(
            title: const Text('Fetch Missing Album Art'),
            subtitle: const Text('Lookup missing art via MusicBrainz'),
            leading: const Icon(Icons.image_search_outlined),
            onTap: () async {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Checking MusicBrainz for album covers...')),
              );
              final albums = await IsarService.instance.getAllAlbums();
              int updated = 0;
              for (final album in albums) {
                if (album.artPath == null && album.onlineArtUrl == null) {
                  final release = await MusicBrainzClient.instance.searchRelease(
                    albumTitle: album.title,
                    artistName: album.artist,
                  );
                  if (release?.coverArtUrl != null) {
                    album.onlineArtUrl = release!.coverArtUrl;
                    album.mbid = release.mbid;
                    updated++;
                  }
                }
              }
              if (updated > 0) {
                await IsarService.instance.upsertAlbums(albums);
              }
              if (context.mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text('Updated $updated album covers.')),
                );
              }
            },
          ),

          const Divider(),

          // ── Downloads & Storage ───────────────────────────────────────────
          const _SectionHeader('STORAGE & DOWNLOADS'),

          ListTile(
            title: const Text('Download Tasks'),
            subtitle: const Text('View and manage audio downloads'),
            leading: const Icon(Icons.download_outlined),
            onTap: () => context.push('/downloads'),
          ),

          const Divider(),

          // ── About ────────────────────────────────────────────────────────
          const _SectionHeader('ABOUT'),

          ListTile(
            title: Text(currentAppName),
            subtitle: Text('Version ${AppConfig.version} (API ${AppConfig.minAndroidApi}+)'),
            leading: const Icon(Icons.info_outline),
          ),
        ],
      ),
    );
  }

  void _showAppNameDialog(
      BuildContext context, WidgetRef ref, String currentName) {
    final controller = TextEditingController(text: currentName);
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Change App Name'),
        content: TextField(
          controller: controller,
          autofocus: true,
          decoration: const InputDecoration(
            hintText: 'Enter new app name',
            labelText: 'App Name',
          ),
        ),
        actions: [
          TextButton(
            onPressed: () {
              ref.read(appNameProvider.notifier).resetDefault();
              Navigator.pop(ctx);
            },
            child: const Text('Reset Default'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () {
              if (controller.text.trim().isNotEmpty) {
                ref
                    .read(appNameProvider.notifier)
                    .setName(controller.text.trim());
              }
              Navigator.pop(ctx);
            },
            child: const Text('Save'),
          ),
        ],
      ),
    );
  }

  void _showThemePicker(
      BuildContext context, WidgetRef ref, AppThemeMode current) {
    showModalBottomSheet(
      context: context,
      builder: (_) => Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Padding(
            padding: EdgeInsets.all(16),
            child: Text('Select Theme',
                style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
          ),
          for (final mode in AppThemeMode.values)
            RadioListTile<AppThemeMode>(
              title: Text(_themeLabel(mode)),
              value: mode,
              groupValue: current,
              onChanged: (v) {
                if (v != null) ref.read(themeProvider.notifier).setTheme(v);
                Navigator.pop(context);
              },
            ),
          const SizedBox(height: 8),
        ],
      ),
    );
  }

  void _showColorPicker(BuildContext context, WidgetRef ref, Color current) {
    Color picked = current;
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Accent color'),
        content: ColorPicker(
          color: picked,
          onColorChanged: (c) => picked = c,
          pickersEnabled: const {
            ColorPickerType.wheel: true,
            ColorPickerType.primary: true,
          },
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () {
              ref.read(accentColorProvider.notifier).setColor(picked.value);
              Navigator.pop(ctx);
            },
            child: const Text('Apply'),
          ),
        ],
      ),
    );
  }

  String _themeLabel(AppThemeMode mode) {
    switch (mode) {
      case AppThemeMode.dotMono:
        return 'Dot Mono (pure black & white)';
      case AppThemeMode.dotAccent:
        return 'Dot + Accent color';
      case AppThemeMode.glass:
        return 'Glassmorphism';
    }
  }
}

class _SectionHeader extends StatelessWidget {
  final String title;
  const _SectionHeader(this.title);

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 4),
      child: Text(
        title,
        style: TextStyle(
          fontSize: 11,
          letterSpacing: 2,
          color: Theme.of(context).colorScheme.primary,
          fontWeight: FontWeight.bold,
        ),
      ),
    );
  }
}

class _LastFmTile extends ConsumerWidget {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return FutureBuilder<String?>(
      future: LastFmAuth.instance.getSavedUsername(),
      builder: (ctx, snap) {
        final username = snap.data;
        return ListTile(
          leading: const Icon(Icons.person_outline),
          title: Text(
            username != null ? 'Logged in as $username' : 'Connect Last.fm',
          ),
          subtitle: username != null
              ? const Text('Scrobbling enabled')
              : const Text('Login to scrobble your tracks'),
          onTap: username != null
              ? () async {
                  await LastFmAuth.instance.logout();
                }
              : () => LastFmAuth.instance.login(context),
        );
      },
    );
  }
}
