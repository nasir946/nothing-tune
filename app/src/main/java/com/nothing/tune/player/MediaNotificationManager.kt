package com.nothing.tune.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.nothing.tune.MainActivity
import com.nothing.tune.R
import com.nothing.tune.data.MusicTrack

object MediaNotificationManager {

    const val CHANNEL_ID = "nothing_tune_playback"
    const val NOTIFICATION_ID = 1001

    const val ACTION_PLAY_PAUSE = "com.nothing.tune.ACTION_PLAY_PAUSE"
    const val ACTION_NEXT = "com.nothing.tune.ACTION_NEXT"
    const val ACTION_PREV = "com.nothing.tune.ACTION_PREV"
    const val ACTION_STOP = "com.nothing.tune.ACTION_STOP"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "NothingTune Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active playback controls in notification panel"
                setShowBadge(false)
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    fun buildNotification(
        context: Context,
        track: MusicTrack,
        isPlaying: Boolean,
        albumArtBitmap: Bitmap? = null
    ): Notification {
        createNotificationChannel(context)

        // Activity intent to open app
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Play/Pause Action
        val playPauseIntent = Intent(context, PlaybackService::class.java).apply {
            action = ACTION_PLAY_PAUSE
        }
        val playPausePending = PendingIntent.getService(
            context,
            1,
            playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Previous Action
        val prevIntent = Intent(context, PlaybackService::class.java).apply {
            action = ACTION_PREV
        }
        val prevPending = PendingIntent.getService(
            context,
            2,
            prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Next Action
        val nextIntent = Intent(context, PlaybackService::class.java).apply {
            action = ACTION_NEXT
        }
        val nextPending = PendingIntent.getService(
            context,
            3,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        val playPauseTitle = if (isPlaying) "Pause" else "Play"

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(track.title)
            .setContentText("${track.artist} • ${track.album}")
            .setSubText(track.quality.name)
            .setLargeIcon(albumArtBitmap)
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .addAction(R.drawable.ic_skip_previous, "Previous", prevPending)
            .addAction(playPauseIcon, playPauseTitle, playPausePending)
            .addAction(R.drawable.ic_skip_next, "Next", nextPending)
            .setStyle(
                MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .build()
    }
}
