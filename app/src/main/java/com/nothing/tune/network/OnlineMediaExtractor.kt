package com.nothing.tune.network

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class ExtractedAudioMedia(
    val title: String,
    val artist: String,
    val streamUrl: String,
    val downloadUrl: String,
    val platform: String,
    val thumbnailUrl: String? = null,
    val durationMs: Long = 0L
)

object OnlineMediaExtractor {

    suspend fun inspectAndExtract(rawUrl: String): Result<ExtractedAudioMedia> = withContext(Dispatchers.IO) {
        val trimmed = rawUrl.trim()
        if (trimmed.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("URL is empty"))
        }

        try {
            when {
                trimmed.contains("youtube.com", ignoreCase = true) || trimmed.contains("youtu.be", ignoreCase = true) -> {
                    extractFromCobaltOrPiped(trimmed, "YouTube")
                }
                trimmed.contains("soundcloud.com", ignoreCase = true) -> {
                    extractFromCobaltOrPiped(trimmed, "SoundCloud")
                }
                else -> {
                    // Direct Audio URL (MP3, FLAC, Radio stream, etc.)
                    val cleanName = trimmed.substringAfterLast('/').substringBefore('?').ifBlank { "online_audio" }
                    val title = cleanName.substringBeforeLast('.').replace("_", " ").replace("-", " ")
                    Result.success(
                        ExtractedAudioMedia(
                            title = title.ifBlank { "Online Stream" },
                            artist = "Live Stream",
                            streamUrl = trimmed,
                            downloadUrl = trimmed,
                            platform = "Direct Audio / Stream"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractFromCobaltOrPiped(targetUrl: String, platformName: String): Result<ExtractedAudioMedia> {
        // 1. Try Cobalt API instances
        val cobaltInstances = listOf(
            "https://cobalt-api.kwiatekm.tokyo/",
            "https://api.cobalt.tools/",
            "https://co.wuk.sh/"
        )

        for (endpoint in cobaltInstances) {
            try {
                val url = URL(endpoint)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("User-Agent", "NothingTune-Android/1.0")
                    connectTimeout = 8000
                    readTimeout = 8000
                    doOutput = true
                }

                val payload = JSONObject().apply {
                    put("url", targetUrl)
                    put("downloadMode", "audio")
                    put("audioFormat", "mp3")
                    put("audioBitrate", "320")
                }

                conn.outputStream.use { os ->
                    os.write(payload.toString().toByteArray())
                }

                if (conn.responseCode in 200..299) {
                    val respText = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(respText)
                    val streamUrl = json.optString("url", "")
                    val filename = json.optString("filename", "audio_track")

                    if (streamUrl.isNotBlank()) {
                        val title = filename.substringBeforeLast('.').replace("_", " ").ifBlank { "$platformName Track" }
                        return Result.success(
                            ExtractedAudioMedia(
                                title = title,
                                artist = platformName,
                                streamUrl = streamUrl,
                                downloadUrl = streamUrl,
                                platform = platformName
                            )
                        )
                    }
                }
            } catch (_: Exception) {
                // Try next endpoint
            }
        }

        // 2. If YouTube, try Piped API as fallback
        if (platformName == "YouTube") {
            try {
                val videoId = extractYouTubeVideoId(targetUrl)
                if (videoId != null) {
                    val pipedUrl = URL("https://pipedapi.kavin.rocks/streams/$videoId")
                    val conn = (pipedUrl.openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        setRequestProperty("Accept", "application/json")
                        connectTimeout = 6000
                        readTimeout = 6000
                    }
                    if (conn.responseCode in 200..299) {
                        val body = conn.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(body)
                        val title = json.optString("title", "YouTube Track")
                        val uploader = json.optString("uploader", "YouTube")
                        val audioStreams = json.optJSONArray("audioStreams")
                        if (audioStreams != null && audioStreams.length() > 0) {
                            val stream = audioStreams.getJSONObject(0).optString("url")
                            if (stream.isNotBlank()) {
                                return Result.success(
                                    ExtractedAudioMedia(
                                        title = title,
                                        artist = uploader,
                                        streamUrl = stream,
                                        downloadUrl = stream,
                                        platform = "YouTube"
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        // 3. Fallback: Treat as direct streamable link
        val fallbackTitle = if (platformName == "YouTube") "YouTube Audio" else "SoundCloud Audio"
        return Result.success(
            ExtractedAudioMedia(
                title = fallbackTitle,
                artist = platformName,
                streamUrl = targetUrl,
                downloadUrl = targetUrl,
                platform = platformName
            )
        )
    }

    private fun extractYouTubeVideoId(url: String): String? {
        return when {
            url.contains("v=") -> url.substringAfter("v=").substringBefore('&')
            url.contains("youtu.be/") -> url.substringAfter("youtu.be/").substringBefore('?')
            url.contains("/shorts/") -> url.substringAfter("/shorts/").substringBefore('?')
            else -> null
        }
    }

    fun startDownload(context: Context, media: ExtractedAudioMedia): Long {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val cleanTitle = media.title.replace(Regex("[^a-zA-Z0-9.-]"), "_")
        val fileName = "$cleanTitle.mp3"

        val request = DownloadManager.Request(Uri.parse(media.downloadUrl)).apply {
            setTitle(media.title)
            setDescription("Downloading from ${media.platform} via NothingTune")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, "NothingTune/$fileName")
            setMimeType("audio/mpeg")
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }

        return downloadManager.enqueue(request)
    }
}
