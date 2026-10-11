import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../config/app_config.dart';

class AppNameNotifier extends Notifier<String> {
  static const _key = 'custom_app_name';

  @override
  String build() {
    _loadAsync();
    return AppConfig.appName;
  }

  Future<void> _loadAsync() async {
    final prefs = await SharedPreferences.getInstance();
    final custom = prefs.getString(_key);
    if (custom != null && custom.trim().isNotEmpty) {
      state = custom.trim();
    }
  }

  Future<void> setName(String newName) async {
    final trimmed = newName.trim();
    if (trimmed.isEmpty) return;
    state = trimmed;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_key, trimmed);
  }

  Future<void> resetDefault() async {
    state = AppConfig.appName;
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_key);
  }
}

final appNameProvider = NotifierProvider<AppNameNotifier, String>(
  AppNameNotifier.new,
);
