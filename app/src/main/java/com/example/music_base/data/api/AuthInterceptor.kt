package com.example.music_base.data.api

import com.example.music_base.data.local.TokenManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            tokenManager.accessToken.firstOrNull()
        }
        
        val requestBuilder = chain.request().newBuilder()
        
        // Only add token if the request doesn't have it already
        if (chain.request().header("Authorization") == null && token != null) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
            android.util.Log.d("AuthInterceptor", "Token injected for: ${chain.request().url} (ends: ...${token.takeLast(8)})")
        } else if (token == null) {
            android.util.Log.w("AuthInterceptor", "NO TOKEN for: ${chain.request().url}")
        }
        
        return chain.proceed(requestBuilder.build())
    }
}
