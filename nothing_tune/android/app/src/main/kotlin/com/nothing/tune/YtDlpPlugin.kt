package com.nothing.tune

import android.content.Context
import android.os.Handler
import android.os.Looper
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.*
import java.io.File

object YtDlpPlugin {

    private const val CHANNEL = "com.nothing.tune/ytdlp"

    private lateinit var appContext: Context
    private lateinit var ytDlpBinary: File
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun register(flutterEngine: FlutterEngine, context: Context) {
        appContext = context.applicationContext

        // Copy yt-dlp binary from assets to files dir on first run
        scope.launch { extractBinary() }

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL
        ).setMethodCallHandler { call, result ->
            when (call.method) {
                "getInfo" -> {
                    val url = call.argument<String>("url") ?: run {
                        result.error("NO_URL", "No URL provided", null)
                        return@setMethodCallHandler
                    }
                    scope.launch {
                        try {
                            val info = getInfo(url)
                            Handler(Looper.getMainLooper()).post { result.success(info) }
                        } catch (e: Exception) {
                            Handler(Looper.getMainLooper()).post {
                                result.error("YTDLP_ERROR", e.message, null)
                            }
                        }
                    }
                }

                "download" -> {
                    val id = call.argument<Int>("id") ?: 0
                    val url = call.argument<String>("url") ?: run {
                        result.error("NO_URL", "No URL provided", null)
                        return@setMethodCallHandler
                    }
                    val quality = call.argument<String>("quality") ?: "best"
                    scope.launch { download(id, url, quality) }
                    result.success(null)
                }

                else -> result.notImplemented()
            }
        }
    }

    private fun extractBinary() {
        val abi = android.os.Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
        val assetName = when {
            abi.startsWith("arm64") -> "bin/yt-dlp-arm64"
            abi.startsWith("armeabi") -> "bin/yt-dlp-arm32"
            else -> "bin/yt-dlp-arm64"
        }

        val outFile = File(appContext.filesDir, "yt-dlp")
        if (!outFile.exists()) {
            try {
                appContext.assets.open(assetName).use { input ->
                    outFile.outputStream().use { output -> input.copyTo(output) }
                }
                outFile.setExecutable(true)
            } catch (_: Exception) {}
        }
        ytDlpBinary = outFile
    }

    private fun getInfo(url: String): Map<String, Any?> {
        val cmd = buildCommand(
            "--dump-json", "--no-playlist",
            "--flat-playlist", url
        )
        val output = runProcess(cmd)
        val json = org.json.JSONObject(output)
        return mapOf(
            "title" to json.optString("title"),
            "uploader" to json.optString("uploader"),
            "thumbnail" to json.optString("thumbnail"),
            "duration" to json.optLong("duration"),
            "url" to json.optString("url"),
            "webpage_url" to json.optString("webpage_url"),
        )
    }

    private fun download(id: Int, url: String, quality: String) {
        val outputDir = File(
            android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_MUSIC
            ), "NothingTune"
        )
        outputDir.mkdirs()

        val formatFlag = when (quality) {
            "flac" -> arrayOf("-f", "bestaudio[ext=flac]/bestaudio", "--extract-audio", "--audio-format", "flac")
            "320k" -> arrayOf("-f", "bestaudio", "--extract-audio", "--audio-format", "mp3", "--audio-quality", "320k")
            "128k" -> arrayOf("-f", "bestaudio", "--extract-audio", "--audio-format", "mp3", "--audio-quality", "128k")
            else   -> arrayOf("-f", "bestaudio/best")
        }

        val outputTemplate = File(outputDir, "%(title)s.%(ext)s").absolutePath

        val cmd = buildCommand(
            *formatFlag,
            "-o", outputTemplate,
            "--no-playlist",
            url
        )

        try {
            runProcess(cmd)
        } catch (_: Exception) {}
    }

    private fun buildCommand(vararg args: String): Array<String> {
        return arrayOf(ytDlpBinary.absolutePath, *args)
    }

    private fun runProcess(cmd: Array<String>): String {
        val process = ProcessBuilder(*cmd)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        process.waitFor()
        return output
    }
}
