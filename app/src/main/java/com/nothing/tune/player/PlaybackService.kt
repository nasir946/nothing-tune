package com.nothing.tune.player

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.nothing.tune.data.MusicTrack

class PlaybackService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            MediaNotificationManager.ACTION_PLAY_PAUSE -> {
                AudioPlayerController.activeInstance?.togglePlayPause()
            }
            MediaNotificationManager.ACTION_NEXT -> {
                AudioPlayerController.activeInstance?.skipNext()
            }
            MediaNotificationManager.ACTION_PREV -> {
                AudioPlayerController.activeInstance?.skipPrevious()
            }
            MediaNotificationManager.ACTION_STOP -> {
                stopForegroundService()
            }
            ACTION_UPDATE_NOTIFICATION -> {
                val currentTrack = AudioPlayerController.activeInstance?.uiState?.value?.currentTrack
                val isPlaying = AudioPlayerController.activeInstance?.uiState?.value?.isPlaying ?: false
                if (currentTrack != null) {
                    val notif = MediaNotificationManager.buildNotification(this, currentTrack, isPlaying)
                    if (isPlaying) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            startForeground(
                                MediaNotificationManager.NOTIFICATION_ID,
                                notif,
                                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                            )
                        } else {
                            startForeground(MediaNotificationManager.NOTIFICATION_ID, notif)
                        }
                    } else {
                        stopForeground(STOP_FOREGROUND_DETACH)
                        NotificationManagerCompat.from(this).notify(MediaNotificationManager.NOTIFICATION_ID, notif)
                    }
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun stopForegroundService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    companion object {
        const val ACTION_UPDATE_NOTIFICATION = "com.nothing.tune.ACTION_UPDATE_NOTIFICATION"

        fun updateNotification(context: Context, track: MusicTrack?, isPlaying: Boolean) {
            if (track == null) return
            try {
                val intent = Intent(context, PlaybackService::class.java).apply {
                    action = ACTION_UPDATE_NOTIFICATION
                }
                if (isPlaying) {
                    ContextCompat.startForegroundService(context, intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Throwable) {}
        }
    }
}
