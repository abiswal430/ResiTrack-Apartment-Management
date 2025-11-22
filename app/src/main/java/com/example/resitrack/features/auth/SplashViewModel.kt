package com.example.resitrack.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.resitrack.data.manager.AuthManager
import com.example.resitrack.data.manager.UsersManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SplashDestination {
    object Login : SplashDestination()
    object AdminDashboard : SplashDestination()
    object ResidentDashboard : SplashDestination()
    object Loading : SplashDestination()
}

class SplashViewModel(
    private val authManager: AuthManager,
    private val usersManager: UsersManager
) : ViewModel() {

    private val _startDestination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val startDestination: StateFlow<SplashDestination> = _startDestination

    init {
        checkUserAuthentication()
    }

    private fun checkUserAuthentication() {
        viewModelScope.launch {
            val uid = authManager.getCurrentUserUid()
            if (uid == null) {
                _startDestination.value = SplashDestination.Login
            } else {
                try {
                    val role = usersManager.getUserRole(uid)
                    when (role) {
                        "admin" -> _startDestination.value = SplashDestination.AdminDashboard
                        "resident" -> _startDestination.value = SplashDestination.ResidentDashboard
                        else -> _startDestination.value = SplashDestination.Login // Fallback
                    }
                } catch (e: Exception) {
                    _startDestination.value = SplashDestination.Login
                }
            }
        }
    }
}

class SplashViewModelFactory(
    private val authManager: AuthManager,
    private val usersManager: UsersManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SplashViewModel(authManager, usersManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
