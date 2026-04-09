package com.example.music_base.data.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import com.example.music_base.data.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import kotlin.concurrent.fixedRateTimer
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

enum class RepeatMode {
    OFF, ALL, ONE
}

object MusicPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private var context: Context? = null
    private var mediaSession: MediaSessionCompat? = null
    private var notificationManager: MusicNotificationManager? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private var queue: List<Track> = emptyList()
    private var originalQueue: List<Track> = emptyList()
    private var currentIndex: Int = -1
    private var timer: Timer? = null

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    fun init(context: Context) {
        if (this.context != null) return // Prevent double initialization
        this.context = context.applicationContext
        setupMediaSession(this.context!!)
        notificationManager = MusicNotificationManager(this.context!!)
    }

    private fun setupMediaSession(context: Context) {
        mediaSession = MediaSessionCompat(context, "MusicPlayerManager").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() = togglePlayPause()
                override fun onPause() = togglePlayPause()
                override fun onSkipToNext() = next()
                override fun onSkipToPrevious() = previous()
                override fun onSeekTo(pos: Long) = seekTo(pos)
                override fun onSetRepeatMode(mode: Int) {
                    toggleRepeat()
                }
                override fun onSetShuffleMode(mode: Int) {
                    toggleShuffle()
                }
            })
            isActive = true
        }
    }

    fun setQueue(tracks: List<Track>, startIndex: Int, initialPositionMs: Long = 0L) {
        if (tracks.isEmpty()) {
            stop()
            return
        }
        originalQueue = tracks
        if (_isShuffle.value) {
            queue = smartShuffle(tracks, startIndex)
            currentIndex = 0
        } else {
            queue = tracks
            currentIndex = startIndex.coerceIn(0, tracks.size - 1)
        }
        playTrack(queue[currentIndex], initialPositionMs)
        updatePlaybackState()
    }


    fun toggleShuffle(likedTrackIds: Set<String> = emptySet()) {
        val currentTrack = _currentTrack.value
        _isShuffle.value = !_isShuffle.value
        
        if (_isShuffle.value) {
            // Turning shuffle ON
            originalQueue = queue
            val index = currentIndex.coerceIn(0, queue.size - 1)
            queue = smartShuffle(queue, index, likedTrackIds)
            currentIndex = 0
        } else {
            // Turning shuffle OFF - return to original order
            val currentId = currentTrack?.id
            queue = originalQueue
            currentIndex = queue.indexOfFirst { it.id == currentId }.coerceAtLeast(0)
        }
        updatePlaybackState()
    }

    private fun smartShuffle(tracks: List<Track>, currentTrackIndex: Int, likedTrackIds: Set<String> = emptySet()): List<Track> {
        if (tracks.isEmpty()) return tracks
        
        val currentTrack = tracks[currentTrackIndex]
        val remainingTracks = tracks.toMutableList().apply { removeAt(currentTrackIndex) }
        
        // Separate tracks into Liked and Others
        val (liked, others) = remainingTracks.partition { likedTrackIds.contains(it.id) }
        
        // Shuffle each pool
        val shuffledLiked = liked.shuffled().toMutableList()
        val shuffledOthers = others.shuffled().toMutableList()
        
        val result = mutableListOf(currentTrack)
        val combinedPool = (shuffledLiked + shuffledOthers).toMutableList()
        
        // Smart interleaving to avoid same artist back-to-back
        var lastArtist = currentTrack.artistName
        
        while (combinedPool.isNotEmpty()) {
            // Try to find a track with a different artist
            val nextTrackIndex = combinedPool.indexOfFirst { it.artistName != lastArtist }
            
            val trackToAdd = if (nextTrackIndex != -1) {
                combinedPool.removeAt(nextTrackIndex)
            } else {
                // If no different artist found, just take the first one
                combinedPool.removeAt(0)
            }
            
            result.add(trackToAdd)
            lastArtist = trackToAdd.artistName
        }
        
        return result
    }

    private fun playTrack(track: Track, initialPosition: Long = 0L) {
        // Reset current states IMMEDIATELY when play is called
        _currentTrack.value = track
        _currentPosition.value = initialPosition
        _duration.value = 0
        _isPlaying.value = false
        
        updateMetadata(track)
        updatePlaybackState()

        val url = track.audioUrl
        if (url.isNullOrBlank()) {
            stopPlayback()
            return
        }

        // Stop timer IMMEDIATELY to prevent accessing player while releasing
        timer?.cancel()
        timer = null

        // Properly release previous player and clear its listeners to prevent callback crashes
        mediaPlayer?.let { player ->
            try {
                player.setOnPreparedListener(null)
                player.setOnCompletionListener(null)
                player.setOnErrorListener(null)
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mediaPlayer = null
        
        val currentContext = context ?: return // Ensure context is not null
        
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(currentContext, Uri.parse(url))
                prepareAsync()
                setOnPreparedListener { mp ->
                    // Verify if this is still the active player instance
                    if (this@MusicPlayerManager.mediaPlayer == mp) {
                        try {
                            if (initialPosition > 0) {
                                mp.seekTo(initialPosition.toInt())
                            }
                            mp.start()
                            _isPlaying.value = true
                            try {
                                _duration.value = mp.duration.toLong()
                            } catch (e: Exception) {
                                android.util.Log.e("MusicPlayerManager", "Failed to get duration", e)
                            }

                            startTimer()
                            updatePlaybackState()
                            mediaSession?.let { session ->
                                try {
                                    notificationManager?.showNotification(track, true, session)
                                } catch (e: Exception) {
                                    android.util.Log.e("MusicPlayerManager", "Failed to show notification", e)
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("MusicPlayerManager", "Error starting playback", e)
                            _isPlaying.value = false
                            updatePlaybackState()
                        }
                    } else {
                        // This instance was already replaced by another next/previous click
                        mp.release()
                    }
                }
                setOnCompletionListener { mp ->
                    if (this@MusicPlayerManager.mediaPlayer == mp) {
                        when (_repeatMode.value) {
                            RepeatMode.ONE -> playTrack(queue[currentIndex]) // Loop same track
                            else -> next()
                        }
                    }
                }
                setOnErrorListener { mp, what, extra ->
                    if (this@MusicPlayerManager.mediaPlayer == mp) {
                        _isPlaying.value = false
                        updatePlaybackState()
                        android.widget.Toast.makeText(currentContext, "Player Error: $what / $extra", android.widget.Toast.LENGTH_LONG).show()
                    }
                    false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isPlaying.value = false
            updatePlaybackState()
        }
    }

    private fun startTimer() {
        timer?.cancel()
        timer = fixedRateTimer(period = 500) { // Faster update (500ms) for smoother UI
            try {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        _currentPosition.value = mp.currentPosition.toLong()
                    }
                }
            } catch (e: Exception) {
                // MediaPlayer might be in a state where isPlaying/currentPosition is illegal
            }
        }
    }

    fun togglePlayPause() {
        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.pause()
                    _isPlaying.value = false
                    mediaSession?.let { session ->
                        _currentTrack.value?.let { track ->
                            notificationManager?.showNotification(track, false, session)
                        }
                    }
                } else {
                    it.start()
                    _isPlaying.value = true
                    mediaSession?.let { session ->
                        _currentTrack.value?.let { track ->
                            notificationManager?.showNotification(track, true, session)
                        }
                    }
                }
                updatePlaybackState()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun stopPlayback() {
        try {
            timer?.cancel()
            timer = null
            mediaPlayer?.let { player ->
                player.setOnPreparedListener(null)
                player.setOnCompletionListener(null)
                player.setOnErrorListener(null)
                if (player.isPlaying) player.stop()
                player.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        stopPlayback()
        notificationManager?.cancelNotification()
        _isPlaying.value = false
        _currentTrack.value = null
        _currentPosition.value = 0
        _duration.value = 0
        queue = emptyList()
        currentIndex = -1
    }

    fun seekTo(position: Long) {
        try {
            mediaPlayer?.seekTo(position.toInt())
            _currentPosition.value = position
            updatePlaybackState()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun next() {
        if (queue.isNotEmpty()) {
            if (currentIndex < queue.size - 1) {
                currentIndex++
                playTrack(queue[currentIndex])
            } else if (_repeatMode.value == RepeatMode.ALL) {
                currentIndex = 0
                playTrack(queue[currentIndex])
            } else {
                _isPlaying.value = false
                try {
                    mediaPlayer?.pause()
                } catch (e: Exception) { /* Ignore */ }
            }
        }
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        updatePlaybackState()
    }

    fun previous() {
        if (queue.isNotEmpty() && currentIndex > 0) {
            currentIndex--
            playTrack(queue[currentIndex])
        }
    }

    fun release() {
        mediaPlayer?.apply {
            setOnPreparedListener(null)
            setOnCompletionListener(null)
            setOnErrorListener(null)
            try {
                if (isPlaying) this.stop() // explicitly call MediaPlayer.stop()
            } catch (e: Exception) { /* Ignore */ }
            release()
        }
        timer?.cancel()
        mediaPlayer = null
        mediaSession?.release()
        mediaSession = null
        notificationManager?.destroy()
        notificationManager = null
    }

    private fun updateMetadata(track: Track) {
        val metadataBuilder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.artistName ?: "Unknown Artist")
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, track.durationMs)
            
        // Note: For album art, we'd ideally load the bitmap from Glide/Coil here,
        // but for now we set the text metadata.
        mediaSession?.setMetadata(metadataBuilder.build())
    }

    private fun updatePlaybackState() {
        val state = if (_isPlaying.value) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        val actions = PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SEEK_TO or
                PlaybackStateCompat.ACTION_SET_REPEAT_MODE or
                PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE

        val stateBuilder = PlaybackStateCompat.Builder()
            .setActions(actions)
            .setState(state, _currentPosition.value, 1.0f)

        mediaSession?.setPlaybackState(stateBuilder.build())

        // Set repeat/shuffle mode directly on the session (not on the builder)
        mediaSession?.setRepeatMode(when(_repeatMode.value) {
            RepeatMode.ALL -> PlaybackStateCompat.REPEAT_MODE_ALL
            RepeatMode.ONE -> PlaybackStateCompat.REPEAT_MODE_ONE
            else -> PlaybackStateCompat.REPEAT_MODE_NONE
        })
        mediaSession?.setShuffleMode(
            if (_isShuffle.value) PlaybackStateCompat.SHUFFLE_MODE_ALL
            else PlaybackStateCompat.SHUFFLE_MODE_NONE
        )
    }
}
