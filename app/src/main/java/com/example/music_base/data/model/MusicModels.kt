package com.example.music_base.data.model

import com.google.gson.annotations.SerializedName

data class Meta(
    val total: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int
)

data class PaginatedResponse<T>(
    val data: List<T>,
    val meta: Meta
)

data class Thumbnail(
    val id: String?,
    val url: String,
    val width: Int?,
    val height: Int?,
    val artistId: String?,
    val trackId: String?
)

data class ArtistSummary(
    val id: String,
    val name: String
)

data class Track(
    val id: String,
    val title: String,
    val youtubeVideoId: String,
    val viewCount: Long,
    val duration: Double, // API returns Double (e.g. 198.001)
    val audioUrl: String?,
    val bitrate: Int?,
    val artistId: String,
    val artist: Artist?,
    val thumbnails: List<Thumbnail>?,
    val currentPosition: Long = 0L
) {
    val coverUrl: String
        get() = thumbnails?.firstOrNull()?.url 
            ?: "https://img.youtube.com/vi/$youtubeVideoId/hqdefault.jpg"
            
    val artistName: String?
        get() = artist?.name
    
    val durationMs: Long
        get() = (duration * 1000).toLong()
        
    val formattedViews: String
        get() = when {
            viewCount >= 1_000_000 -> "%.1fM".format(viewCount / 1_000_000.0)
            viewCount >= 1_000 -> "%.1fK".format(viewCount / 1_000.0)
            else -> "$viewCount"
        } + " views"
}

data class Artist(
    val id: String,
    val name: String,
    val youtubeChannelId: String,
    val uploaderId: String,
    val description: String?,
    val thumbnails: List<Thumbnail>?,
    val createdAt: String,
    val updatedAt: String
) {
    val imageUrl: String
        get() = thumbnails?.firstOrNull()?.url 
            ?: "https://picsum.photos/800/800?random=$id"
}

// Deprecated or Mock models used by old UI
data class AlbumContentCount(
    val tracks: Int
)

data class Album(
    val id: String,
    val title: String,
    val description: String?,
    val thumbnail: String?,
    val artistId: String,
    val artist: Artist?,
    val releaseDate: String?,
    @SerializedName("_count")
    val count: AlbumContentCount?,
    val createdAt: String,
    val updatedAt: String
) {
    val coverUrl: String
        get() = thumbnail ?: "https://picsum.photos/800/800?random=$id"
}

data class AlbumDetail(
    val id: String,
    val title: String,
    val description: String?,
    val thumbnail: String?,
    val artistId: String,
    val artist: Artist?,
    val tracks: List<Track>?,
    val releaseDate: String?,
    val createdAt: String,
    val updatedAt: String
) {
    val coverUrl: String
        get() = thumbnail ?: "https://picsum.photos/800/800?random=$id"
}

data class Playlist(
    val id: String,
    val name: String,
    val userId: String? = null,
    val description: String? = null,
    val isPublic: Boolean = true,
    @SerializedName("_count")
    val count: PlaylistCount? = null
) {
    val trackCount: Int
        get() = count?.tracks ?: 0
        
    val coverUrl: String
        get() = "https://picsum.photos/800/800?random=$id"
}

data class PlaylistCount(
    val tracks: Int = 0
)

data class PlaylistDetail(
    val id: String,
    val name: String,
    val description: String?,
    val userId: String,
    val isPublic: Boolean,
    val tracks: PlaylistTracksResponse?
)

data class PlaylistTracksResponse(
    val data: List<PlaylistTrackEntry>,
    val meta: Meta?
)

data class PlaylistTrackEntry(
    val playlistId: String,
    val trackId: String,
    val track: Track
)

data class FollowResponse(
    val userId: String,
    val artistId: String,
    val createdAt: String,
    val artist: Artist
)

data class CreatePlaylistRequest(
    val name: String,
    val description: String?,
    val isPublic: Boolean = true
)

data class AddTrackRequest(
    val trackId: String
)

data class ListenRequest(
    val trackId: String,
    val currentTime: Int,
    val duration: Int
)

data class PlaybackHistoryItem(
    val id: String,
    val trackId: String,
    val trackName: String,
    val trackThumb: String?,
    val listenedSeconds: Int,
    val trackDuration: Int,
    val completed: Boolean,
    @SerializedName("created_at")
    val createdAt: String,
    val track: Track?
)

data class PlaybackDateGroup(
    val date: String,
    val items: List<PlaybackHistoryItem>
)

data class InsightData(
    val streak: Int,
    val preferredTimeOfDay: String,
    val totalSecondsLast7: Int = 0,
    val dailyStats: List<DailyStat> = emptyList()
)

data class DailyStat(
    val date: String,
    val seconds: Int
)

data class TopArtistInfo(
    val artist: Artist,
    val playCount: Int
)

data class PlaybackHistoryResponse(
    val data: List<PlaybackDateGroup>,
    val meta: Meta
)
