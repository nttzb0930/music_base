package com.example.music_base.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.music_base.data.model.*
import com.example.music_base.data.repository.MusicRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.async

class MusicViewModel(
    private val repository: MusicRepository
) : ViewModel() {
    class Factory(private val repository: MusicRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
                return MusicViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    private val _uiState = MutableStateFlow<MusicState>(MusicState.Loading)
    val uiState: StateFlow<MusicState> = _uiState.asStateFlow()

    private val _albumDetail = MutableStateFlow<AlbumDetail?>(null)
    val albumDetail: StateFlow<AlbumDetail?> = _albumDetail.asStateFlow()

    private val _isDetailLoading = MutableStateFlow(false)
    val isDetailLoading: StateFlow<Boolean> = _isDetailLoading.asStateFlow()

    private val _playlistDetail = MutableStateFlow<PlaylistDetail?>(null)
    val playlistDetail: StateFlow<PlaylistDetail?> = _playlistDetail.asStateFlow()

    private val _playlistDetailError = MutableStateFlow<String?>(null)
    val playlistDetailError: StateFlow<String?> = _playlistDetailError.asStateFlow()

    private val _isPlaylistDetailLoading = MutableStateFlow(false)
    val isPlaylistDetailLoading: StateFlow<Boolean> = _isPlaylistDetailLoading.asStateFlow()

    private val _artistTracks = MutableStateFlow<List<Track>>(emptyList())
    val artistTracks: StateFlow<List<Track>> = _artistTracks.asStateFlow()

    private val _isArtistDetailLoading = MutableStateFlow(false)
    val isArtistDetailLoading: StateFlow<Boolean> = _isArtistDetailLoading.asStateFlow()

    private val _suggestedTracks = MutableStateFlow<List<Track>>(emptyList())
    val suggestedTracks: StateFlow<List<Track>> = _suggestedTracks.asStateFlow()

    private val _rankingTracks = MutableStateFlow<List<Track>>(emptyList())
    val rankingTracks: StateFlow<List<Track>> = _rankingTracks.asStateFlow()

    private val _followedArtists = MutableStateFlow<List<Artist>>(emptyList())
    val followedArtists: StateFlow<List<Artist>> = _followedArtists.asStateFlow()

    private val _followedArtistIds = MutableStateFlow<Set<String>>(emptySet())
    val followedArtistIds: StateFlow<Set<String>> = _followedArtistIds.asStateFlow()
    
    private val _userPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val userPlaylists: StateFlow<List<Playlist>> = _userPlaylists.asStateFlow()

    private val _suggestedPage = MutableStateFlow(1)
    private val _rankingPage = MutableStateFlow(1)
    private val _albumsPage = MutableStateFlow(1)
    private val _artistsPage = MutableStateFlow(1)
    
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Track>>(emptyList())
    val searchResults: StateFlow<List<Track>> = _searchResults.asStateFlow()

    private val _likedTracks = MutableStateFlow<List<Track>>(emptyList())
    val likedTracks: StateFlow<List<Track>> = _likedTracks.asStateFlow()

    private val _likedTrackIds = MutableStateFlow<Set<String>>(emptySet())
    val likedTrackIds: StateFlow<Set<String>> = _likedTrackIds.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    private val _playbackHistory = MutableStateFlow<List<PlaybackDateGroup>>(emptyList())
    val playbackHistory: StateFlow<List<PlaybackDateGroup>> = _playbackHistory.asStateFlow()

    private val _isHistoryLoading = MutableStateFlow(false)
    val isHistoryLoading: StateFlow<Boolean> = _isHistoryLoading.asStateFlow()

    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    val filteredHistory = combine(_playbackHistory, _historySearchQuery) { history, query ->
        if (query.isBlank()) history
        else {
            history.map { group ->
                group.copy(items = group.items.filter { 
                    it.trackName.contains(query, ignoreCase = true) || 
                    (it.track?.artistName?.contains(query, ignoreCase = true) == true)
                })
            }.filter { it.items.isNotEmpty() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Recently played tracks (unique, most recent first) with progress info.
     * Used by the "Continue Listening" section on the Home screen.
     */
    val recentlyPlayedTracks = _playbackHistory.map { history ->
        history
            .flatMap { group -> group.items }
            .distinctBy { it.trackId }          // keep only first (most-recent) occurrence
            .take(10)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topArtistsFromHistory = _playbackHistory.map { history ->
        history.flatMap { group -> group.items }
            .mapNotNull { it.track?.artist }
            .groupBy { it.id }
            .map { entry -> 
                val artist = entry.value.first()
                TopArtistInfo(artist, entry.value.size)
            }
            .sortedByDescending { it.playCount }
            .take(10)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    val listeningInsights = _playbackHistory.map { history ->
        if (history.isEmpty()) return@map null
        
        // 1. Calculate Streak
        val dates = history.map { it.date }.distinct().sortedDescending()
        var streak = 0
        if (dates.isNotEmpty()) {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val today = java.util.Calendar.getInstance()
            val todayStr = sdf.format(today.time)
            
            // Start checking from the most recent date in history
            var currentCheck = today
            if (dates.first() == todayStr || dates.first() == sdf.format(java.util.Calendar.getInstance().apply { add(java.util.Calendar.DATE, -1) }.time)) {
                // Determine start of streak
                var checkDate = if (dates.first() == todayStr) today else java.util.Calendar.getInstance().apply { add(java.util.Calendar.DATE, -1) }
                
                for (dateStr in dates) {
                    if (dateStr == sdf.format(checkDate.time)) {
                        streak++
                        checkDate.add(java.util.Calendar.DATE, -1)
                    } else break
                }
            }
        }

        // 2. Calculate Active Hours
        val items = history.flatMap { it.items }
        val hours = items.mapNotNull { item ->
            try {
                // Assuming createdAt is "2026-04-09T10:30:00Z"
                val timePart = item.createdAt.split("T").getOrNull(1) ?: return@mapNotNull null
                timePart.split(":").firstOrNull()?.toInt()
            } catch (e: Exception) { null }
        }
        
        val timeOfDay = if (hours.isEmpty()) "Unknown"
        else {
            val morning = hours.count { it in 5..11 }
            val afternoon = hours.count { it in 12..17 }
            val evening = hours.count { it in 18..21 }
            val night = hours.count { it >= 22 || it < 5 }
            
            mapOf("Morning" to morning, "Afternoon" to afternoon, "Evening" to evening, "Night" to night)
                .maxByOrNull { it.value }?.key ?: "Unknown"
        }

        // 3. Calculate Weekly Stats (Last 7 Days)
        val last7DaysStats = history.take(7).map { group ->
            DailyStat(group.date, group.items.sumOf { it.listenedSeconds })
        }
        val totalSecondsLast7 = last7DaysStats.sumOf { it.seconds }

        InsightData(streak, timeOfDay, totalSecondsLast7, last7DaysStats)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private var lastRecordedSeconds = -1
    private var lastRecordedTrackId = ""

    private val _trackCache = mutableMapOf<String, Track>()

    private fun updateTrackCache(tracks: List<Track>) {
        tracks.forEach { track ->
            val existing = _trackCache[track.id]
            // Only update if existing is null OR if new track has audioUrl while existing doesn't
            if (existing == null || (!track.audioUrl.isNullOrBlank() && existing.audioUrl.isNullOrBlank())) {
                _trackCache[track.id] = track
            }
        }
    }

    private fun filterTracks(tracks: List<Track>): List<Track> {
        val filtered = tracks.filter { 
            !it.audioUrl.isNullOrBlank() && 
            it.duration > 50.0 && 
            it.viewCount > 100000 
        }
        Log.d("MusicVM", "filterTracks: raw=${tracks.size}, filtered=${filtered.size} (Criteria: audioUrl!=null, duration>50s, views>100k)")
        return filtered
    }

    /** Lighter filter: only requires a valid audioUrl. Used for Album/Artist/Playlist/Ranking. */
    private fun filterPlayableTracks(tracks: List<Track>): List<Track> {
        return tracks.filter { !it.audioUrl.isNullOrBlank() }
    }

    private suspend fun fetchTracksUntilSatisfied(
        targetCount: Int,
        maxPages: Int = 10,
        fetcher: suspend (page: Int) -> Result<PaginatedResponse<Track>>
    ): List<Track> {
        val resultList = mutableListOf<Track>()
        var currentPage = 1
        
        while (resultList.size < targetCount && currentPage <= maxPages) {
            val result = fetcher(currentPage)
            val response = result.getOrNull() ?: break
            
            val filtered = filterTracks(response.data)
            resultList.addAll(filtered)
            
            if (response.data.size < 10 || currentPage >= response.meta.totalPages) break
            
            currentPage++
        }
        
        return resultList.distinctBy { it.id }.take(targetCount)
    }

    private fun filterAlbums(albums: List<Album>): List<Album> {
        val filtered = albums
            .filter { (it.count?.tracks ?: 0) > 0 }
            .sortedByDescending { it.count?.tracks ?: 0 }
        Log.d("MusicVM", "filterAlbums: raw=${albums.size}, filtered=${filtered.size}")
        return filtered
    }

    init {
        loadData()
        loadLikedTracks()
        loadPlaybackHistory()
    }


    fun getAlbumDetail(id: String) {
        viewModelScope.launch {
            _isDetailLoading.value = true
            _albumDetail.value = null
            repository.getAlbumDetail(id).fold(
                onSuccess = { detail ->
                    val filteredTracks = detail.tracks?.let { filterPlayableTracks(it) }
                    _albumDetail.value = detail.copy(tracks = filteredTracks)
                    filteredTracks?.let { updateTrackCache(it) }
                },
                onFailure = { /* Handle error */ }
            )
            _isDetailLoading.value = false
        }
    }

    suspend fun fetchTrackDetail(id: String): Track? {
        val cached = _trackCache[id]
        if (cached != null && !cached.audioUrl.isNullOrBlank()) {
            return cached
        }
        val result = repository.getTrackDetail(id).getOrNull()
        result?.let { updateTrackCache(listOf(it)) }
        return result
    }

    fun getArtistTracks(id: String) {
        viewModelScope.launch {
            _isArtistDetailLoading.value = true
            _artistTracks.value = emptyList()
            repository.getArtistTracks(id).fold(
                onSuccess = { response ->
                    val filtered = filterPlayableTracks(response.data)
                    _artistTracks.value = filtered
                    updateTrackCache(filtered)
                },
                onFailure = { /* Handle error */ }
            )
            _isArtistDetailLoading.value = false
        }
    }

    fun loadMoreSuggestedTracks(limit: Int = 10) {
        if (_isLoadingMore.value) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            val startPage = _suggestedPage.value + 1
            val current = _suggestedTracks.value
            val newTracks = fetchTracksUntilSatisfied(limit, maxPages = 10) { page ->
                repository.getTracks(page = startPage + (page - 1), limit = limit, sortBy = "viewCount", sort = "desc")
            }
            val existing = current.map { it.id }.toSet()
            val unique = newTracks.filter { it.id !in existing }
            if (unique.isNotEmpty()) {
                _suggestedTracks.value = current + unique
                _suggestedPage.value = startPage
                updateTrackCache(unique)
            }
            _isLoadingMore.value = false
        }
    }

    fun loadMoreRankingTracks(limit: Int = 10) {
        if (_isLoadingMore.value) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            val nextPage = _rankingPage.value + 1
            repository.getRankingTracks(page = nextPage, limit = limit).fold(
                onSuccess = { response ->
                    if (response.data.isNotEmpty()) {
                        val filtered = filterPlayableTracks(response.data)
                        _rankingTracks.value = _rankingTracks.value + filtered
                        _rankingPage.value = nextPage
                        updateTrackCache(filtered)
                    }
                },
                onFailure = { }
            )
            _isLoadingMore.value = false
        }
    }

    fun loadMoreAlbums(limit: Int = 10) {
        if (_isLoadingMore.value) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            val nextPage = _albumsPage.value + 1
            repository.getAlbums(page = nextPage, limit = limit).fold(
                onSuccess = { response ->
                    if (response.data.isNotEmpty()) {
                        val filtered = filterAlbums(response.data)
                        val currentSuccess = _uiState.value as? MusicState.Success
                        if (currentSuccess != null) {
                            _uiState.value = currentSuccess.copy(
                                albums = currentSuccess.albums + filtered
                            )
                        }
                        _albumsPage.value = nextPage
                    }
                },
                onFailure = { }
            )
            _isLoadingMore.value = false
        }
    }

    
    // Token is now handled automatically by AuthInterceptor — no token param needed
    fun loadMyPlaylists() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.getMyPlaylists(isPublic = null, page = 1, limit = 50).fold(
                onSuccess = { 
                    _userPlaylists.value = it.data 
                },
                onFailure = { 
                    _toastMessage.emit("Failed to load playlists: ${it.message}")
                }
            )
            _isRefreshing.value = false
        }
    }

    fun createPlaylist(name: String, description: String? = null, isPublic: Boolean = false) {
        viewModelScope.launch {
            repository.createPlaylist(name, description, isPublic).fold(
                onSuccess = { newPlaylist ->
                    _toastMessage.emit("Playlist created successfully")
                    loadMyPlaylists()
                },
                onFailure = { 
                    _toastMessage.emit("Error: ${it.message}")
                }
            )
        }
    }

    fun getPlaylistDetail(id: String) {
        viewModelScope.launch {
            _isPlaylistDetailLoading.value = true
            _playlistDetailError.value = null
            // Clear only if switching to a different playlist to prevent flashing old data
            if (_playlistDetail.value?.id != id) {
                _playlistDetail.value = null
            }
            repository.getPlaylistDetail(id).fold(
                onSuccess = { detail ->
                    val filteredEntries = detail.tracks?.data?.filter { !it.track.audioUrl.isNullOrBlank() }
                    val filteredDetail = detail.copy(
                        tracks = detail.tracks?.copy(data = filteredEntries ?: emptyList())
                    )
                    _playlistDetail.value = filteredDetail
                    filteredEntries?.let { entries ->
                        updateTrackCache(entries.map { it.track })
                    }
                },
                onFailure = { 
                    _playlistDetailError.value = it.message
                }
            )
            _isPlaylistDetailLoading.value = false
        }
    }

    fun updatePlaylist(id: String, name: String?, description: String?, isPublic: Boolean?) {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.updatePlaylist(id, name, description, isPublic).fold(
                onSuccess = { 
                    _toastMessage.emit("Playlist updated")
                    loadMyPlaylists()
                    getPlaylistDetail(id)
                },
                onFailure = { _toastMessage.emit(it.message ?: "Update failed") }
            )
            _isRefreshing.value = false
        }
    }

    fun deletePlaylist(id: String) {
        viewModelScope.launch {
            repository.deletePlaylist(id).fold(
                onSuccess = { 
                    _toastMessage.emit("Playlist deleted")
                    loadMyPlaylists()
                },
                onFailure = { 
                    _toastMessage.emit(it.message ?: "Failed to delete playlist")
                }
            )
        }
    }
    
    fun addTrackToPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, trackId).fold(
                onSuccess = { 
                    _toastMessage.emit("Added to playlist")
                    loadMyPlaylists()
                    // Refresh detail if we are currently viewing this playlist
                    if (_playlistDetail.value?.id == playlistId) {
                        getPlaylistDetail(playlistId)
                    }
                },
                onFailure = { 
                    _toastMessage.emit(it.message ?: "Failed to add track")
                }
            )
        }
    }

    fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistId, trackId).fold(
                onSuccess = { 
                    _toastMessage.emit("Removed from playlist")
                    // Refresh detail if we are currently viewing this playlist
                    if (_playlistDetail.value?.id == playlistId) {
                        getPlaylistDetail(playlistId)
                    }
                },
                onFailure = { 
                    _toastMessage.emit(it.message ?: "Failed to remove track")
                }
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.trim().length >= 2) {
            searchTracks(query)
        } else if (query.trim().isEmpty()) {
            _searchResults.value = emptyList()
        }
    }

    private fun searchTracks(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            repository.getTracks(query = query, limit = 20).fold(
                onSuccess = { response ->
                    val filtered = filterTracks(response.data)
                    _searchResults.value = filtered
                    updateTrackCache(filtered)
                },
                onFailure = { /* Handle error */ }
            )
            _isSearching.value = false
        }
    }

    fun followArtist(artistId: String) {
        viewModelScope.launch {
            repository.followArtist(artistId).fold(
                onSuccess = { 
                    _followedArtistIds.value = _followedArtistIds.value + artistId
                    loadFollowedArtists()
                },
                onFailure = { /* Handle error */ }
            )
        }
    }

    fun unfollowArtist(artistId: String) {
        viewModelScope.launch {
            repository.unfollowArtist(artistId).fold(
                onSuccess = { 
                    _followedArtistIds.value = _followedArtistIds.value - artistId
                    loadFollowedArtists()
                },
                onFailure = { /* Handle error */ }
            )
        }
    }

    fun loadFollowedArtists() {
        viewModelScope.launch {
            repository.getFollowedArtists().fold(
                onSuccess = { follows ->
                    _followedArtists.value = follows.map { it.artist }
                    _followedArtistIds.value = follows.map { it.artistId }.toSet()
                },
                onFailure = { /* Handle error */ }
            )
        }
    }

    fun loadSuggestedTracks() {
        viewModelScope.launch {
            val tracks = fetchTracksUntilSatisfied(10) { page -> 
                repository.getTracks(page = page, limit = 10, sortBy = "viewCount", sort = "desc") 
            }
            if (tracks.isNotEmpty()) {
                _suggestedTracks.value = tracks.shuffled()
                updateTrackCache(tracks)
            }
        }
    }

    fun loadLikedTracks(query: String? = null) {
        viewModelScope.launch {
            val validQuery = if (query.isNullOrBlank()) null else query
            repository.getLikedTracks(page = 1, limit = 100, query = validQuery).fold(
                onSuccess = { response ->
                    val filtered = filterPlayableTracks(response.data)
                    _likedTracks.value = filtered
                    if (validQuery == null) {
                        _likedTrackIds.value = filtered.map { track -> track.id }.toSet()
                    }
                },
                onFailure = { }
            )
        }
    }

    fun toggleTrackLike(trackId: String) {
        viewModelScope.launch {
            repository.toggleTrackLike(trackId).fold(
                onSuccess = { response ->
                    if (response.liked) {
                        _likedTrackIds.value = _likedTrackIds.value + trackId
                        _toastMessage.emit("Added to Liked Songs")
                    } else {
                        _likedTrackIds.value = _likedTrackIds.value - trackId
                        _toastMessage.emit("Removed from Liked Songs")
                    }
                    loadLikedTracks() // Refresh list
                },
                onFailure = { _toastMessage.emit("Failed to toggle like: ${it.message}") }
            )
        }
    }

    fun clearAllLikedTracks() {
        viewModelScope.launch {
            repository.clearAllLikedTracks().fold(
                onSuccess = {
                    _likedTrackIds.value = emptySet()
                    _likedTracks.value = emptyList()
                    _toastMessage.emit("All liked tracks removed")
                },
                onFailure = { _toastMessage.emit("Failed to clear liked tracks: ${it.message}") }
            )
        }
    }

    fun resetPagination() {
        _suggestedPage.value = 1
        _rankingPage.value = 1
        _albumsPage.value = 1
        _artistsPage.value = 1
        _isLoadingMore.value = false
    }

    fun loadMoreSuggestedTracks() {
        if (_isLoadingMore.value) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            val startPage = _suggestedPage.value + 1
            val current = _suggestedTracks.value
            val newTracks = fetchTracksUntilSatisfied(10, maxPages = 10) { page ->
                repository.getTracks(page = startPage + (page - 1), limit = 10, sortBy = "viewCount", sort = "desc")
            }
            val existing = current.map { it.id }.toSet()
            val unique = newTracks.filter { it.id !in existing }
            if (unique.isNotEmpty()) {
                _suggestedTracks.value = current + unique
                _suggestedPage.value = startPage
                updateTrackCache(unique)
            }
            _isLoadingMore.value = false
        }
    }

    fun loadMoreRankingTracks() {
        if (_isLoadingMore.value) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            val nextPage = _rankingPage.value + 1
            repository.getRankingTracks(page = nextPage).fold(
                onSuccess = { response ->
                    val filtered = filterPlayableTracks(response.data)
                    if (filtered.isNotEmpty()) {
                        _rankingTracks.value = _rankingTracks.value + filtered
                        _rankingPage.value = nextPage
                        updateTrackCache(filtered)
                    }
                },
                onFailure = { /* Handle error */ }
            )
            _isLoadingMore.value = false
        }
    }

    fun loadMoreAlbums() {
        if (_isLoadingMore.value) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            repository.getAlbums(page = _albumsPage.value + 1).fold(
                onSuccess = { response ->
                    val filtered = filterAlbums(response.data)
                    if (filtered.isNotEmpty()) {
                        val current = (_uiState.value as? MusicState.Success) ?: return@fold
                        _uiState.value = current.copy(albums = current.albums + filtered)
                        _albumsPage.value += 1
                    }
                },
                onFailure = { /* Handle error */ }
            )
            _isLoadingMore.value = false
        }
    }

    fun loadMoreArtists() {
        if (_isLoadingMore.value) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            repository.getArtists(page = _artistsPage.value + 1).fold(
                onSuccess = { response ->
                    if (response.data.isNotEmpty()) {
                        val current = (_uiState.value as? MusicState.Success) ?: return@fold
                        _uiState.value = current.copy(artists = current.artists + response.data)
                        _artistsPage.value += 1
                    }
                },
                onFailure = { /* Handle error */ }
            )
            _isLoadingMore.value = false
        }
    }

    fun loadData() {
        viewModelScope.launch {
            if (_uiState.value !is MusicState.Success) {
                _uiState.value = MusicState.Loading
            }
            
            val tracksDeferred = async { 
                fetchTracksUntilSatisfied(10) { page -> 
                    repository.getTracks(page = page, limit = 10, sortBy = "viewCount", sort = "desc") 
                } 
            }
            val albumsDeferred = async { repository.getAlbums(limit = 40) }
            val artistsDeferred = async { repository.getArtists(limit = 40) }
            val rankingDeferred = async { 
                fetchTracksUntilSatisfied(10) { page -> 
                    repository.getRankingTracks(page = page, limit = 10) 
                } 
            }

            val tracks = tracksDeferred.await()
            val albumsResult = albumsDeferred.await()
            val artistsResult = artistsDeferred.await()
            val ranking = rankingDeferred.await()

            val albums = albumsResult.getOrNull()?.data ?: emptyList()
            val artists = artistsResult.getOrNull()?.data ?: emptyList()

            if (tracks.isNotEmpty() || albums.isNotEmpty() || artists.isNotEmpty() || ranking.isNotEmpty()) {
                _suggestedTracks.value = tracks
                _rankingTracks.value = ranking
                updateTrackCache(tracks)
                updateTrackCache(ranking)
                
                _uiState.value = MusicState.Success(
                    tracks = tracks,
                    albums = filterAlbums(albums),
                    artists = artists
                )
            } else {
                _uiState.value = MusicState.Error("No data available")
            }
        }
    }

    // --- PLAYBACK ---
    fun recordPlayback(trackId: String, currentTimeMs: Long, durationMs: Long) {
        val currentSec = (currentTimeMs / 1000).toInt()
        val durationSec = (durationMs / 1000).toInt()

        // Only record if 10 seconds passed or track changed, to avoid spam
        if (trackId == lastRecordedTrackId && (currentSec - lastRecordedSeconds) < 10 && currentSec < durationSec * 0.9) {
            return
        }

        if (currentSec < 1) return

        viewModelScope.launch {
            repository.recordPlayback(trackId, currentSec, durationSec).fold(
                onSuccess = { 
                    lastRecordedSeconds = currentSec
                    lastRecordedTrackId = trackId
                },
                onFailure = { /* Silent fail for background sync */ }
            )
        }
    }

    fun loadPlaybackHistory() {
        viewModelScope.launch {
            _isHistoryLoading.value = true
            repository.getPlaybackHistory().fold(
                onSuccess = { 
                    _playbackHistory.value = it.data
                    it.data.forEach { group ->
                        updateTrackCache(group.items.mapNotNull { it.track })
                    }
                },
                onFailure = { }
            )
            _isHistoryLoading.value = false
        }
    }

    fun onHistorySearchQueryChanged(query: String) {
        _historySearchQuery.value = query
    }

    fun removeTrackFromHistory(trackId: String) {
        viewModelScope.launch {
            repository.removeTrackFromHistory(trackId).fold(
                onSuccess = { loadPlaybackHistory() },
                onFailure = { }
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory().fold(
                onSuccess = { _playbackHistory.value = emptyList() },
                onFailure = { }
            )
        }
    }

    fun setToastMessage(message: String) {
        viewModelScope.launch {
            _toastMessage.emit(message)
        }
    }
}




