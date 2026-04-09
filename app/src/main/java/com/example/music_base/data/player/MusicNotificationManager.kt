package com.example.music_base.data.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.example.music_base.MainActivity
import com.example.music_base.R
import com.example.music_base.data.model.Track
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicNotificationManager(private val context: Context) {
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    private val channelId = "music_playback_channel"
    private val notificationId = 101
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls for music playback"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(track: Track, isPlaying: Boolean, mediaSession: MediaSessionCompat) {
        val playPauseIcon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        val playPauseTitle = if (isPlaying) "Pause" else "Play"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_play) // Use play as small icon
            .setContentTitle(track.title)
            .setContentText(track.artistName ?: "Unknown Artist")
            .setLargeIcon(null) // Will update with bitmap soon
            .setContentIntent(pendingIntent)
            .setOngoing(isPlaying)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .addAction(
                R.drawable.ic_skip_previous, "Previous",
                createPendingIntent(PlaybackAction.PREVIOUS)
            )
            .addAction(
                playPauseIcon, playPauseTitle,
                createPendingIntent(PlaybackAction.PLAY_PAUSE)
            )
            .addAction(
                R.drawable.ic_skip_next, "Next",
                createPendingIntent(PlaybackAction.NEXT)
            )

        // Show immediate notification
        notificationManager.notify(notificationId, builder.build())

        // Load album art asynchronously using managed scope
        if (!track.coverUrl.isNullOrBlank()) {
            scope.launch {
                val bitmap = loadBitmap(track.coverUrl)
                if (bitmap != null) {
                    withContext(Dispatchers.Main) {
                        builder.setLargeIcon(bitmap)
                        notificationManager.notify(notificationId, builder.build())
                    }
                }
            }
        }
    }

    fun cancelNotification() {
        notificationManager.cancel(notificationId)
    }

    fun destroy() {
        scope.cancel()
    }

    private fun createPendingIntent(action: PlaybackAction): PendingIntent {
        val intent = Intent(context, MusicActionReceiver::class.java).apply {
            this.action = action.name
        }
        return PendingIntent.getBroadcast(
            context, action.ordinal, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private suspend fun loadBitmap(url: String): Bitmap? {
        // Use coil's ImageLoader more efficiently
        val loader = context.imageLoader
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false) // Required for getting a software bitmap we can draw to a canvas
            .build()
        val result = try {
            loader.execute(request)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        return (result as? SuccessResult)?.drawable?.let { drawable ->
            try {
                val size = 512
                val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, size, size)
                drawable.draw(canvas)
                bitmap
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

enum class PlaybackAction {
    PLAY_PAUSE, NEXT, PREVIOUS
}
