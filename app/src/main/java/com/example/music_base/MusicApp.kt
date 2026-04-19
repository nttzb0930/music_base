package com.example.music_base

import android.app.Application
import com.example.music_base.data.api.StashApiService
import com.example.music_base.data.local.TokenManager
import com.example.music_base.data.repository.AuthRepository
import com.example.music_base.data.repository.MusicRepository
import com.example.music_base.data.repository.StashRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MusicApp : Application() {
    lateinit var authRepository: AuthRepository
    lateinit var musicRepository: MusicRepository
    lateinit var stashRepository: StashRepository
    lateinit var tokenManager: TokenManager

    override fun onCreate() {
        super.onCreate()
        
        tokenManager = TokenManager(this)
        
        val okHttpClient = okhttp3.OkHttpClient.Builder()
            .addInterceptor(com.example.music_base.data.api.AuthInterceptor(tokenManager))
            .authenticator(com.example.music_base.data.api.TokenAuthenticator(tokenManager))
            .build()
            
        val retrofit = Retrofit.Builder()
            .baseUrl(com.example.music_base.data.api.AuthApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            
        val authApiService = retrofit.create(com.example.music_base.data.api.AuthApiService::class.java)
        val musicApiService = retrofit.create(com.example.music_base.data.api.MusicApiService::class.java)
        val stashApiService = retrofit.create(com.example.music_base.data.api.StashApiService::class.java)
        
        authRepository = AuthRepository(authApiService, tokenManager)
        musicRepository = MusicRepository(musicApiService)
        stashRepository = StashRepository(stashApiService)
        
        com.example.music_base.data.player.MusicPlayerManager.init(this)
    }
}


