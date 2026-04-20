package com.example.music_base.data.api

import com.example.music_base.data.model.*
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

data class UpdatePlaylistRequest(
    val name: String? = null,
    val description: String? = null,
    val isPublic: Boolean? = null
)

data class TrackLikeRequest(
    val trackId: String
)

data class UpdateTrackMetadataRequest(
    val title: String? = null,
    val description: String? = null,
    val artistId: String? = null,
    val albumId: String? = null,
    val thumbnailUrl: String? = null,
    val youtubeVideoId: String? = null
)

data class TrackLikeResponse(
    val liked: Boolean
)

interface MusicApiService {
    @GET("tracks")
    suspend fun getTracks(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("q") query: String? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("sort") sort: String? = null,
        @Query("artistId") artistId: String? = null,
        @Query("albumId") albumId: String? = null
    ): Response<PaginatedResponse<Track>>

    @GET("tracks/audio")
    suspend fun getAudioTracks(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("q") query: String? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("sort") sort: String? = null,
        @Query("artistId") artistId: String? = null,
        @Query("albumId") albumId: String? = null
    ): Response<PaginatedResponse<Track>>

    @GET("tracks/{id}")
    suspend fun getTrackDetail(@Path("id") id: String): Response<Track>

    @PATCH("tracks/{id}/views")
    suspend fun incrementTrackViews(@Path("id") id: String): Response<MessageResponse>

    @GET("tracks/ranking")
    suspend fun getRankingTracks(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("q") query: String? = null
    ): Response<PaginatedResponse<Track>>

    // --- ALBUMS ---
    @GET("albums")
    suspend fun getAlbums(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedResponse<Album>>

    @GET("albums/{id}")
    suspend fun getAlbumDetail(@Path("id") id: String): Response<AlbumDetail>

    @GET("albums/artist/{artistId}")
    suspend fun getArtistAlbums(
        @Path("artistId") artistId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedResponse<Album>>

    // --- ARTISTS ---
    @GET("artists")
    suspend fun getArtists(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedResponse<Artist>>

    @GET("artists/{id}")
    suspend fun getArtistDetail(@Path("id") id: String): Response<Artist>

    @GET("artists/{id}/tracks")
    suspend fun getArtistTracks(
        @Path("id") id: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedResponse<Track>>

    @POST("artists/sync")
    suspend fun syncArtist(
        @Body request: Map<String, Any>
    ): Response<MessageResponse>

    @POST("tracks/sync-url")
    suspend fun syncTrackFromUrl(
        @Body request: Map<String, String>
    ): Response<MessageResponse>

    // --- USERS / FOLLOWS ---
    @GET("users/me/follows")
    suspend fun getFollowedArtists(): Response<List<FollowResponse>>

    @POST("users/me/follows/{artistId}")
    suspend fun followArtist(
        @Path("artistId") artistId: String
    ): Response<MessageResponse>

    @DELETE("users/me/follows/{artistId}")
    suspend fun unfollowArtist(
        @Path("artistId") artistId: String
    ): Response<MessageResponse>

    // --- PLAYLISTS ---
    @POST("playlists")
    suspend fun createPlaylist(
        @Body request: CreatePlaylistRequest
    ): Response<Playlist>

    @GET("playlists/me")
    suspend fun getMyPlaylists(
        @Query("isPublic") isPublic: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<PaginatedResponse<Playlist>>

    @GET("playlists")
    suspend fun getAllPlaylists(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<PaginatedResponse<Playlist>>

    @PATCH("playlists/{id}")
    suspend fun updatePlaylist(
        @Path("id") id: String,
        @Body request: UpdatePlaylistRequest
    ): Response<Playlist>

    @GET("playlists/{id}")
    suspend fun getPlaylistDetail(
        @Path("id") id: String
    ): Response<PlaylistDetail>

    @POST("playlists/{id}/tracks")
    suspend fun addTrackToPlaylist(
        @Path("id") id: String,
        @Body request: AddTrackRequest
    ): Response<MessageResponse>

    @DELETE("playlists/{id}/tracks/{trackId}")
    suspend fun removeTrackFromPlaylist(
        @Path("id") id: String,
        @Path("trackId") trackId: String
    ): Response<MessageResponse>

    @DELETE("playlists/{id}")
    suspend fun deletePlaylist(
        @Path("id") id: String
    ): Response<MessageResponse>

    // --- AUDIO ---
    @Multipart
    @POST("tracks/upload/{trackId}")
    suspend fun uploadAudio(
        @Path("trackId") trackId: String,
        @Part file: okhttp3.MultipartBody.Part
    ): Response<Track>

    // --- TRACK LIKE ---
    @POST("track-like")
    suspend fun toggleTrackLike(
        @Body request: TrackLikeRequest
    ): Response<TrackLikeResponse>

    @GET("track-like")
    suspend fun getLikedTracks(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("q") query: String? = null,
        @Query("sortBy") sortBy: String? = "added_at",
        @Query("sort") sort: String? = "desc"
    ): Response<PaginatedResponse<Track>>

    @DELETE("track-like")
    suspend fun clearAllLikedTracks(): Response<MessageResponse>

    // --- PLAYBACK ---
    @POST("playback/listen")
    suspend fun recordPlayback(@Body request: ListenRequest): Response<MessageResponse>

    @GET("playback/history")
    suspend fun getPlaybackHistory(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("q") query: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<PlaybackHistoryResponse>

    @DELETE("playback/history/{trackId}")
    suspend fun removeTrackFromHistory(@Path("trackId") trackId: String): Response<MessageResponse>

    @DELETE("playback/history")
    suspend fun clearHistory(): Response<MessageResponse>

    // --- ADMIN TRACKS ---
    @Multipart
    @POST("tracks/admin/upload")
    suspend fun adminUploadTrack(
        @Part("title") title: okhttp3.RequestBody,
        @Part("description") description: okhttp3.RequestBody?,
        @Part("artistId") artistId: okhttp3.RequestBody,
        @Part("albumId") albumId: okhttp3.RequestBody?,
        @Part("thumbnailUrl") thumbnailUrl: okhttp3.RequestBody?,
        @Part("youtubeVideoId") youtubeVideoId: okhttp3.RequestBody?,
        @Part("sourceType") sourceType: okhttp3.RequestBody,
        @Part("youtubeUrl") youtubeUrl: okhttp3.RequestBody?,
        @Part file: okhttp3.MultipartBody.Part?
    ): Response<Track>

    @PATCH("tracks/admin/{trackId}")
    suspend fun adminUpdateTrack(
        @Path("trackId") trackId: String,
        @Body request: UpdateTrackMetadataRequest
    ): Response<Track>

    @DELETE("tracks/admin/{trackId}")
    suspend fun adminDeleteTrack(
        @Path("trackId") trackId: String
    ): Response<MessageResponse>
}
