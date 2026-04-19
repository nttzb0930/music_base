package com.example.music_base.data.api

import com.example.music_base.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface StashApiService {
    @GET("stash/overview")
    suspend fun getOverview(): Response<StashOverview>

    @GET("stash/recent")
    suspend fun getRecent(
        @Query("limit") limit: Int = 5
    ): Response<StashRecent>

    @GET("stash/top-tracks")
    suspend fun getTopTracks(
        @Query("limit") limit: Int = 10
    ): Response<StashTopTracks>
}
