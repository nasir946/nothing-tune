import 'package:audio_service/audio_service.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'app/router.dart';
import 'app/theme/app_theme.dart';
import 'audio/audio_handler.dart';
import 'audio/audio_player_service.dart';
import 'config/app_config.dart';
import 'data/database/isar_service.dart';
import 'providers/audio_provider.dart';
import 'providers/theme_provider.dart';
import 'providers/app_name_provider.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Lock to portrait + landscape (allow freeform resize)
  await SystemChrome.setPreferredOrientations([
    DeviceOrientation.portraitUp,
    DeviceOrientation.landscapeLeft,
    DeviceOrientation.landscapeRight,
  ]);

  // Edge-to-edge display
  SystemChrome.setSystemUIOverlayStyle(
    const SystemUiOverlayStyle(
      statusBarColor: Colors.transparent,
      statusBarIconBrightness: Brightness.light,
      systemNavigationBarColor: Colors.black,
    ),
  );

  // Initialise database
  await IsarService.instance.init();

  // Initialise audio service
  audioPlayerService = AudioPlayerService();
  audioHandler = await AudioService.init(
    builder: () => NothingAudioHandler(audioPlayerService),
    config: const AudioServiceConfig(
      androidNotificationChannelId: 'com.nothing.tune.channel.audio',
      androidNotificationChannelName: AppConfig.appName,
      androidNotificationOngoing: true,
      androidStopForegroundOnPause: true,
    ),
  );

  runApp(const ProviderScope(child: NothingTuneApp()));
}

class NothingTuneApp extends ConsumerWidget {
  const NothingTuneApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final appName = ref.watch(appNameProvider);
    final themeMode = ref.watch(themeProvider);
    final accentArgb = ref.watch(accentColorProvider);
    final accentColor = Color(accentArgb);

    return MaterialApp.router(
      title: appName,
      debugShowCheckedModeBanner: false,
      theme: AppTheme.resolve(themeMode, accentColor),
      routerConfig: appRouter,
    );
  }
}
