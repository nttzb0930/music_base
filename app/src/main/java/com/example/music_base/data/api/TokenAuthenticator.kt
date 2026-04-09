package com.example.music_base.data.api

import com.example.music_base.data.local.TokenManager
import com.example.music_base.data.model.AuthResponse
import com.example.music_base.data.model.RefreshRequest
import com.google.gson.Gson
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class TokenAuthenticator(private val tokenManager: TokenManager) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = runBlocking {
            tokenManager.refreshToken.firstOrNull()
        } ?: return null

        // Synchronization is important to prevent multiple refresh calls simultaneously
        synchronized(this) {
            val currentToken = runBlocking { tokenManager.accessToken.firstOrNull() }
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")

            // Check if the token we sent is different from the current one. 
            // If it is, another request might have already refreshed it.
            if (requestToken != currentToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            // Perform synchronous refresh call
            val newTokens = refreshToken(refreshToken)
            return if (newTokens != null) {
                runBlocking {
                    tokenManager.saveTokens(newTokens.accessToken, newTokens.refreshToken)
                }
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${newTokens.accessToken}")
                    .build()
            } else {
                runBlocking { tokenManager.clearTokens() }
                null // Give up and logout
            }
        }
    }

    private fun refreshToken(refreshToken: String): AuthResponse? {
        val client = OkHttpClient()
        val requestBody = Gson().toJson(RefreshRequest(refreshToken))
            .toRequestBody("application/json".toMediaType())
            
        val request = Request.Builder()
            .url("${AuthApiService.BASE_URL}auth/refresh")
            .post(requestBody)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Gson().fromJson(response.body?.string(), AuthResponse::class.java)
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }
}
