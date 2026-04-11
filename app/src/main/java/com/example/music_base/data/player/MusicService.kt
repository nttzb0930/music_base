package com.example.music_base.data.player

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * A background service to monitor the app lifecycle.
 * Specifically handles onTaskRemoved to clean up music playback and notifications
 * when the user swipes away the app from recent tasks.
 */
class MusicService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Clean up when swiped away
        MusicPlayerManager.stop()
        stopSelf()
    }
}
