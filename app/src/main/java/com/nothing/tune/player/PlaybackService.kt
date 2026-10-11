package com.nothing.tune.player

import android.content.Intent
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return AudioPlayerController.activeInstance?.mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = AudioPlayerController.activeInstance?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
