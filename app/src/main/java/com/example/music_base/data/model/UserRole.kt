package com.example.music_base.data.model

/**
 * Represents the available user roles in the system.
 * Using an enum prevents hardcoded string errors and centralizes role management.
 */
enum class UserRole(val value: String) {
    ADMIN("admin"),
    USER("user"),
    ARTIST("artist"),
    GUEST("guest");

    companion object {
        /**
         * Converts a string received from the API into a [UserRole].
         * Defaults to [USER] if the string is unknown.
         */
        fun fromString(value: String?): UserRole {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: USER
        }
    }
}
