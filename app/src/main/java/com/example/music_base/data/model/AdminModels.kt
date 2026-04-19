package com.example.music_base.data.model

data class StashOverview(
    val totalUsers: Int,
    val totalArtists: Int,
    val totalTracks: Int,
    val totalTracksWithAudio: Int,
    val totalAlbums: Int,
    val totalPlaylists: Int,
    val totalFollows: Int,
    val totalTrackLikes: Int,
    val totalPlaybackHistory: Int,
    val newUsersLast7Days: Int,
    val newArtistsLast7Days: Int,
    val newTracksLast7Days: Int,
    val newAlbumsLast7Days: Int,
    val newPlaylistsLast7Days: Int
)

data class StashRecent(
    val limit: Int,
    val tracks: List<StashTrackItem>,
    val artists: List<StashArtistItem>,
    val albums: List<StashAlbumItem>,
    val users: List<StashUserItem>,
    val playlists: List<StashPlaylistItem>
)

data class StashTrackItem(
    val id: String,
    val title: String,
    val viewCount: Long?,
    val artistId: String,
    val artistName: String,
    val audioUrl: String?,
    val createdAt: String
)

data class StashArtistItem(
    val id: String,
    val name: String,
    val youtubeChannelId: String,
    val createdAt: String
)

data class StashAlbumItem(
    val id: String,
    val title: String,
    val artistId: String,
    val artistName: String,
    val createdAt: String
)

data class StashUserItem(
    val id: String,
    val email: String,
    val username: String,
    val role: String,
    val createdAt: String
)

data class StashPlaylistItem(
    val id: String,
    val name: String,
    val userId: String,
    val isPublic: Boolean,
    val createdAt: String
)

data class StashTopTracks(
    val limit: Int,
    val data: List<StashTrackItem>
)
