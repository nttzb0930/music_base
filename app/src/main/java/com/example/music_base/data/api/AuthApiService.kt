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

    companion object {
        const val BASE_URL = "https://api-music-player.up.railway.app/api/v1/"
    }
}
