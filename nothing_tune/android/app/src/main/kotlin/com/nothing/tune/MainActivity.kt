package com.nothing.tune

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine

class MainActivity : FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MediaStorePlugin.register(flutterEngine, this)
        YtDlpPlugin.register(flutterEngine, this)
        EqualizerPlugin.register(flutterEngine)
        HiResAudioPlugin.register(flutterEngine)
    }
}
