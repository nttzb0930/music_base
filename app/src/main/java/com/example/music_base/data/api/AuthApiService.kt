package com.example.music_base.data.api

import com.example.music_base.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<User>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // logout & getMe use token from AuthInterceptor automatically
    @POST("auth/logout")
    suspend fun logout(): Response<MessageResponse>

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): Response<AuthResponse>

    @GET("users/me")
    suspend fun getMe(): Response<User>

    @GET("users")
    suspend fun getAllUsers(): Response<List<User>>

    @PATCH("users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<User>

    @PATCH("users/me/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<MessageResponse>

    companion object {
        const val BASE_URL = "https://api-music-player.up.railway.app/api/v1/"
    }
}
