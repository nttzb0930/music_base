package com.example.music_base.data.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MusicActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        
        when (action) {
            PlaybackAction.PLAY_PAUSE.name -> {
                MusicPlayerManager.togglePlayPause()
            }
            PlaybackAction.NEXT.name -> {
                MusicPlayerManager.next()
            }
            PlaybackAction.PREVIOUS.name -> {
                MusicPlayerManager.previous()
            }
        }
    }
}
