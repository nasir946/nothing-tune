import 'package:shared_preferences/shared_preferences.dart';
import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

/// Handles Last.fm OAuth web-based login.
/// The user is directed to Last.fm in a browser and the token is
/// retrieved after they approve. Full mobile OAuth requires a callback URL
/// scheme registered in AndroidManifest — using the simplified token auth flow here.
class LastFmAuth {
  LastFmAuth._();
  static final LastFmAuth instance = LastFmAuth._();

  static const _usernameKey = 'lastfm_username';
  static const _sessionKey = 'lastfm_session_key';

  Future<String?> getSavedUsername() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_usernameKey);
  }

  Future<String?> getSessionKey() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_sessionKey);
  }

  /// Opens Last.fm in a browser for the user to authorize.
  /// Simplified: user pastes their session key manually.
  Future<void> login(BuildContext context) async {
    final keyController = TextEditingController();
    final nameController = TextEditingController();

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Last.fm Login'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Enter your Last.fm username and API session key.\n'
              'Get a session key via Last.fm API auth flow.',
              style: TextStyle(fontSize: 12),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: nameController,
              decoration: const InputDecoration(hintText: 'Username'),
            ),
            const SizedBox(height: 8),
            TextField(
              controller: keyController,
              decoration: const InputDecoration(hintText: 'Session key'),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () async {
              if (nameController.text.isEmpty || keyController.text.isEmpty) {
                return;
              }
              final prefs = await SharedPreferences.getInstance();
              await prefs.setString(_usernameKey, nameController.text.trim());
              await prefs.setString(_sessionKey, keyController.text.trim());
              if (ctx.mounted) Navigator.pop(ctx);
            },
            child: const Text('Save'),
          ),
        ],
      ),
    );
  }

  Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_usernameKey);
    await prefs.remove(_sessionKey);
  }
}
