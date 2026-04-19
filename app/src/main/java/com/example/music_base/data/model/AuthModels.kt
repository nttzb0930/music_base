package com.example.music_base.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("role") val role: String? = null
) {
    /**
     * Centralized role check to avoid hardcoded string comparisons across the app.
     */
    val userRole: UserRole
        get() = UserRole.fromString(role)

    val isAdmin: Boolean
        get() = userRole == UserRole.ADMIN

    val displayName: String
        get() = when {
            !username.isNullOrBlank() -> username
            !email.isNullOrBlank() -> email.split("@").first()
            else -> "Guest"
        }
}

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String,
    val confirmPassword: String
)

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String
)

data class RefreshRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

data class MessageResponse(
    val message: String
)
