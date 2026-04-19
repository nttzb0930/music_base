package com.example.music_base.data.repository

import com.example.music_base.data.api.StashApiService
import com.example.music_base.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class StashRepository(private val apiService: StashApiService) {

    suspend fun getOverview(): Result<StashOverview> {
        return try {
            val response = apiService.getOverview()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch overview: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecent(limit: Int = 5): Result<StashRecent> {
        return try {
            val response = apiService.getRecent(limit)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch recent data: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTopTracks(limit: Int = 10): Result<StashTopTracks> {
        return try {
            val response = apiService.getTopTracks(limit)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch top tracks: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
