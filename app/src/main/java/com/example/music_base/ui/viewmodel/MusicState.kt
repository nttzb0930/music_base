package com.example.music_base.ui.viewmodel

import com.example.music_base.data.model.Album
import com.example.music_base.data.model.Artist
import com.example.music_base.data.model.Track

sealed class MusicState {
    object Loading : MusicState()
    data class Success(
        val tracks: List<Track>,
        val artists: List<Artist>,
        val albums: List<Album>
    ) : MusicState()
    data class Error(val message: String) : MusicState()
}
