package com.example.music_base.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.music_base.data.model.LoginRequest
import com.example.music_base.data.model.RegisterRequest
import com.example.music_base.data.model.User
import com.example.music_base.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object InitialLoading : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: User) : AuthState()
    object Unauthenticated : AuthState()
    object RegistrationSuccess : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    val currentUser: StateFlow<User?> = _authState
        .map { (it as? AuthState.Authenticated)?.user }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    val accessToken = repository.accessToken

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            _authState.value = AuthState.InitialLoading
            val result = repository.getMe()
            if (result.isSuccess) {
                _authState.value = AuthState.Authenticated(result.getOrThrow())
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.login(LoginRequest(email, pass))
            if (result.isSuccess) {
                checkAuthStatus()
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun register(email: String, username: String, pass: String, confirmPass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.register(RegisterRequest(email, username, pass, confirmPass))
            if (result.isSuccess) {
                _authState.value = AuthState.RegistrationSuccess
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun resetToIdle() {
        _authState.value = AuthState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _authState.value = AuthState.Unauthenticated
        }
    }

    class Factory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(repository) as T
        }
    }
}
