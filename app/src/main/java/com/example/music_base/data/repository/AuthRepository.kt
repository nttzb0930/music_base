package com.example.music_base.data.repository

import com.example.music_base.data.api.AuthApiService
import com.example.music_base.data.local.TokenManager
import com.example.music_base.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import retrofit2.Response

class AuthRepository(
    private val apiService: AuthApiService,
    private val tokenManager: TokenManager
) {
    val accessToken: Flow<String?> = tokenManager.accessToken
    val refreshToken: Flow<String?> = tokenManager.refreshToken

    suspend fun register(request: RegisterRequest): Result<User> {
        return try {
            val response = apiService.register(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return try {
            val response = apiService.login(request)
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
                Result.success(authResponse)
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
            
            val gson = com.google.gson.Gson()
            val jsonObject = gson.fromJson(errorBody, com.google.gson.JsonObject::class.java)
            
            if (jsonObject.has("message")) {
                val messageElement = jsonObject.get("message")
                if (messageElement.isJsonArray) {
                    // Extract all messages from array
                    val messages = messageElement.asJsonArray.map { it.asString }
                    return messages.joinToString(". ")
                } else if (messageElement.isJsonPrimitive) {
                    val msg = messageElement.asString
                    if (msg.isNotBlank()) return msg
                }
            }
            
            // Fallback to "error" field
            if (jsonObject.has("error")) {
                val err = jsonObject.get("error").asString
                if (err.isNotBlank()) return err
            }
            
            response.message().ifBlank { "Request failed (${response.code()})" }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Error parsing error body", e)
            response.message().ifBlank { "Authentication failed" }
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            apiService.logout() // Token from AuthInterceptor
            tokenManager.clearTokens()
            Result.success(Unit)
        } catch (e: Exception) {
            tokenManager.clearTokens()
            Result.success(Unit)
        }
    }

    suspend fun refreshTokens(): Result<AuthResponse> {
        return try {
            val currentRefreshToken = tokenManager.refreshToken.firstOrNull()
            if (currentRefreshToken == null) return Result.failure(Exception("No refresh token"))
            
            val response = apiService.refresh(RefreshRequest(currentRefreshToken))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
                Result.success(authResponse)
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMe(): Result<User> {
        return try {
            val response = apiService.getMe() // Token from AuthInterceptor
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
