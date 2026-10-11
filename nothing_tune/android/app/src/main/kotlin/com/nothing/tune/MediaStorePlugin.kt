package com.nothing.tune

import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

object MediaStorePlugin {

    private const val CHANNEL = "com.nothing.tune/media_store"

    fun register(flutterEngine: FlutterEngine, context: Context) {
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL
        ).setMethodCallHandler { call, result ->
            when (call.method) {
                "scanMusic" -> {
                    Thread {
                        try {
                            val songs = scanMusic(context)
                            result.success(songs)
                        } catch (e: Exception) {
                            result.error("SCAN_ERROR", e.message, null)
                        }
                    }.start()
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun scanMusic(context: Context): List<Map<String, Any?>> {
        val songs = mutableListOf<Map<String, Any?>>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.GENRE,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.BITRATE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.ALBUM_ID,
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 OR " +
                "${MediaStore.Audio.Media.IS_AUDIOBOOK} != 0 OR " +
                "${MediaStore.Audio.Media.IS_PODCAST} != 0"

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor? = context.contentResolver.query(
            uri, projection, selection, null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )

        cursor?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val pathCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val genreCol = c.getColumnIndex(MediaStore.Audio.Media.GENRE)
            val trackCol = c.getColumnIndex(MediaStore.Audio.Media.TRACK)
            val yearCol = c.getColumnIndex(MediaStore.Audio.Media.YEAR)
            val durCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val bitrateCol = c.getColumnIndex(MediaStore.Audio.Media.BITRATE)
            val dateCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val albumIdCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (c.moveToNext()) {
                val path = c.getString(pathCol) ?: continue
                val id = c.getLong(idCol)
                val albumId = c.getLong(albumIdCol)
                val bitrate = if (bitrateCol >= 0) c.getInt(bitrateCol) else null

                // Extract extended metadata via MediaMetadataRetriever
                var sampleRate: Int? = null
                var bitDepth: Int? = null
                var codec: String? = null
                var artPath: String? = null

                try {
                    val mmr = MediaMetadataRetriever()
                    mmr.setDataSource(path)
                    sampleRate = mmr.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_SAMPLERATE
                    )?.toIntOrNull()
                    bitDepth = mmr.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_BITS_PER_SAMPLE
                    )?.toIntOrNull()
                    codec = mmr.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_MIMETYPE
                    )?.substringAfterLast('/')?.uppercase()

                    // Extract embedded art to cache
                    val art = mmr.embeddedPicture
                    if (art != null) {
                        val cacheDir = context.cacheDir
                        val artFile = java.io.File(cacheDir, "art_$id.jpg")
                        if (!artFile.exists()) {
                            artFile.writeBytes(art)
                        }
                        artPath = artFile.absolutePath
                    }
                    mmr.release()
                } catch (_: Exception) {}

                // Album art via MediaStore AlbumArt
                if (artPath == null) {
                    val artUri = Uri.parse(
                        "content://media/external/audio/albumart/$albumId"
                    )
                    artPath = artUri.toString()
                }

                songs.add(
                    mapOf(
                        "path" to path,
                        "title" to c.getString(titleCol),
                        "artist" to c.getString(artistCol),
                        "album" to c.getString(albumCol),
                        "genre" to (if (genreCol >= 0) c.getString(genreCol) else null),
                        "track" to (if (trackCol >= 0) c.getInt(trackCol) else null),
                        "year" to (if (yearCol >= 0) c.getInt(yearCol) else null),
                        "duration" to c.getInt(durCol),
                        "bitrate" to bitrate?.div(1000), // convert bps → kbps
                        "sampleRate" to sampleRate,
                        "bitDepth" to bitDepth,
                        "codec" to codec,
                        "dateAdded" to c.getLong(dateCol) * 1000L,
                        "artPath" to artPath,
                    )
                )
            }
        }

        return songs
    }
}
