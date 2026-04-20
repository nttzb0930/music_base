package com.example.music_base.data.repository

import com.example.music_base.data.api.MusicApiService
import com.example.music_base.data.api.UpdatePlaylistRequest
import com.example.music_base.data.api.TrackLikeRequest
import com.example.music_base.data.api.TrackLikeResponse
import com.example.music_base.data.model.*
import com.example.music_base.data.model.MessageResponse
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import android.util.Log

class MusicRepository(private val apiService: MusicApiService) {

    suspend fun getTracks(
        page: Int = 1,
        limit: Int = 10,
        query: String? = null,
        sortBy: String? = null,
        sort: String? = null,
        artistId: String? = null,
        albumId: String? = null
    ): Result<PaginatedResponse<Track>> {
        return handleApi { apiService.getTracks(page, limit, query, sortBy, sort, artistId, albumId) }
    }

    suspend fun getAudioTracks(
        page: Int = 1,
        limit: Int = 10,
        query: String? = null,
        sortBy: String? = null,
        sort: String? = null,
        artistId: String? = null,
        albumId: String? = null
    ): Result<PaginatedResponse<Track>> {
        return handleApi { apiService.getAudioTracks(page, limit, query, sortBy, sort, artistId, albumId) }
    }

    suspend fun getTrackDetail(id: String): Result<Track> {
        return handleApi { apiService.getTrackDetail(id) }
    }

    suspend fun incrementTrackViews(id: String): Result<MessageResponse> {
        return handleApi { apiService.incrementTrackViews(id) }
    }

    suspend fun getRankingTracks(page: Int = 1, limit: Int = 10, query: String? = null): Result<PaginatedResponse<Track>> {
        return handleApi { apiService.getRankingTracks(page, limit, query) }
    }

    // --- ALBUMS ---
    suspend fun getAlbums(page: Int = 1, limit: Int = 20): Result<PaginatedResponse<Album>> {
        return handleApi { apiService.getAlbums(page, limit) }
    }

    suspend fun getAlbumDetail(id: String): Result<AlbumDetail> {
        return handleApi { apiService.getAlbumDetail(id) }
    }

    suspend fun getArtistAlbums(artistId: String, page: Int = 1, limit: Int = 20): Result<PaginatedResponse<Album>> {
        return handleApi { apiService.getArtistAlbums(artistId, page, limit) }
    }

    // --- ARTISTS ---
    suspend fun getArtists(page: Int = 1, limit: Int = 20): Result<PaginatedResponse<Artist>> {
        return handleApi { apiService.getArtists(page, limit) }
    }

    suspend fun getArtistDetail(id: String): Result<Artist> {
        return handleApi { apiService.getArtistDetail(id) }
    }

    suspend fun getArtistTracks(id: String, page: Int = 1, limit: Int = 20): Result<PaginatedResponse<Track>> {
        return handleApi { apiService.getArtistTracks(id, page, limit) }
    }

    // --- USERS / FOLLOWS ---
    // Token is injected automatically by AuthInterceptor
    suspend fun getFollowedArtists(): Result<List<FollowResponse>> {
        return handleApi { apiService.getFollowedArtists() }
    }

    suspend fun followArtist(artistId: String): Result<MessageResponse> {
        return handleApi { apiService.followArtist(artistId) }
    }

    suspend fun unfollowArtist(artistId: String): Result<MessageResponse> {
        return handleApi { apiService.unfollowArtist(artistId) }
    }

    // --- PLAYLISTS ---
    // Token is injected automatically by AuthInterceptor
    suspend fun createPlaylist(name: String, description: String?, isPublic: Boolean): Result<Playlist> {
        return handleApi { apiService.createPlaylist(CreatePlaylistRequest(name, description, isPublic)) }
    }

    suspend fun getMyPlaylists(isPublic: Boolean? = null, page: Int = 1, limit: Int = 50): Result<PaginatedResponse<Playlist>> {
        return handleApi { apiService.getMyPlaylists(isPublic, page, limit) }
    }

    suspend fun updatePlaylist(id: String, name: String?, description: String?, isPublic: Boolean?): Result<Playlist> {
        return handleApi { apiService.updatePlaylist(id,
            UpdatePlaylistRequest(name, description, isPublic)
        ) }
    }

    suspend fun getPlaylistDetail(id: String): Result<PlaylistDetail> {
        return try {
            val response = apiService.getPlaylistDetail(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Result.success(body)
                else Result.failure(Exception("Empty body"))
            } else {
                val err = response.errorBody()?.string()
                Result.failure(Exception(err ?: "HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addTrackToPlaylist(id: String, trackId: String): Result<MessageResponse> {
        return handleApi { apiService.addTrackToPlaylist(id, AddTrackRequest(trackId)) }
    }

    suspend fun removeTrackFromPlaylist(id: String, trackId: String): Result<MessageResponse> {
        return handleApi { apiService.removeTrackFromPlaylist(id, trackId) }
    }

    suspend fun deletePlaylist(id: String): Result<MessageResponse> {
        return handleApi { apiService.deletePlaylist(id) }
    }

    // --- AUDIO ---
    suspend fun uploadAudio(trackId: String, file: okhttp3.MultipartBody.Part): Result<Track> {
        return handleApi { apiService.uploadAudio(trackId, file) }
    }

    // --- TRACK LIKE ---
    suspend fun toggleTrackLike(trackId: String): Result<TrackLikeResponse> {
        return handleApi { apiService.toggleTrackLike(TrackLikeRequest(trackId)) }
    }

    suspend fun syncTrackFromUrl(url: String): Result<MessageResponse> {
        return handleApi { apiService.syncTrackFromUrl(mapOf("url" to url)) }
    }

    suspend fun getLikedTracks(
        page: Int = 1,
        limit: Int = 50,
        query: String? = null,
        sortBy: String? = "added_at",
        sort: String? = "desc"
    ): Result<PaginatedResponse<Track>> {
        return handleApi { apiService.getLikedTracks(page, limit, query, sortBy, sort) }
    }

    suspend fun clearAllLikedTracks(): Result<MessageResponse> {
        return handleApi { apiService.clearAllLikedTracks() }
    }

    // --- PLAYBACK ---
    suspend fun recordPlayback(trackId: String, currentTime: Int, duration: Int): Result<MessageResponse> {
        return handleApi { apiService.recordPlayback(ListenRequest(trackId, currentTime, duration)) }
    }

    suspend fun getPlaybackHistory(
        page: Int = 1,
        limit: Int = 10,
        query: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Result<PlaybackHistoryResponse> {
        return handleApi { apiService.getPlaybackHistory(page, limit, query, startDate, endDate) }
    }

    suspend fun removeTrackFromHistory(trackId: String): Result<MessageResponse> {
        return handleApi { apiService.removeTrackFromHistory(trackId) }
    }

    suspend fun clearHistory(): Result<MessageResponse> {
        return handleApi { apiService.clearHistory() }
    }

    // --- ADMIN TRACKS ---
    suspend fun adminUploadTrack(
        title: String,
        description: String?,
        artistId: String,
        albumId: String?,
        thumbnailUrl: String?,
        youtubeVideoId: String?,
        sourceType: String,
        youtubeUrl: String?,
        file: okhttp3.MultipartBody.Part?
    ): Result<Track> {
        val mediaType = "text/plain".toMediaTypeOrNull()
        val titleRB = title.toRequestBody(mediaType)
        val descRB = description?.toRequestBody(mediaType)
        val artistRB = artistId.toRequestBody(mediaType)
        val albumRB = albumId?.toRequestBody(mediaType)
        val thumbRB = thumbnailUrl?.toRequestBody(mediaType)
        val ytVidRB = youtubeVideoId?.toRequestBody(mediaType)
        val sourceRB = sourceType.toRequestBody(mediaType)
        val ytUrlRB = youtubeUrl?.toRequestBody(mediaType)

        return handleApi {
            val response = apiService.adminUploadTrack(
                titleRB, descRB, artistRB, albumRB, thumbRB, ytVidRB, sourceRB, ytUrlRB, file
            )
            if (response.isSuccessful) {
                Log.d("MusicRepository", "Upload success: ${response.code()}")
            } else {
                Log.e("MusicRepository", "Upload failed: ${response.code()} - ${response.errorBody()?.string()}")
            }
            response
        }
    }

    suspend fun adminUpdateTrack(
        trackId: String,
        title: String?,
        description: String?,
        artistId: String?,
        albumId: String?,
        thumbnailUrl: String?,
        youtubeVideoId: String?
    ): Result<Track> {
        return handleApi {
            apiService.adminUpdateTrack(
                trackId,
                com.example.music_base.data.api.UpdateTrackMetadataRequest(title, description, artistId, albumId, thumbnailUrl, youtubeVideoId)
            )
        }
    }

    suspend fun adminDeleteTrack(trackId: String): Result<MessageResponse> {
        return handleApi { apiService.adminDeleteTrack(trackId) }
    }

    private suspend fun <T> handleApi(call: suspend () -> Response<T>): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    @Suppress("UNCHECKED_CAST")
                    Result.success(null as T)
                }
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getErrorMessage(response: Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody.isNullOrBlank()) return response.message().ifBlank { "HTTP ${response.code()}" }
            
            val gson = Gson()
            val jsonObject = gson.fromJson(errorBody, com.google.gson.JsonObject::class.java)
            
            if (jsonObject.has("message")) {
                val messageElement = jsonObject.get("message")
                if (messageElement.isJsonArray) {
                    val messages = messageElement.asJsonArray.map { it.asString }
                    return messages.joinToString(". ")
                } else if (messageElement.isJsonPrimitive) {
                    val msg = messageElement.asString
                    if (msg.isNotBlank()) return msg
                }
            }
            
            if (jsonObject.has("error")) {
                val err = jsonObject.get("error").asString
                if (err.isNotBlank()) return err
            }
            
            response.message().ifBlank { "Request failed (${response.code()})" }
        } catch (e: Exception) {
            response.message().ifBlank { "Request failed" }
        }
    }
}
