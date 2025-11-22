package com.example.resitrack.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.resitrack.data.manager.AuthManager
import com.example.resitrack.data.manager.UsersManager
import com.example.resitrack.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true
)

class ProfileViewModel(
    private val usersManager: UsersManager,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val userId = authManager.getCurrentUserId()
            if (userId != null) {
                val userDetails = usersManager.getResidentDetails(userId)
                _uiState.update { it.copy(user = userDetails, isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false) } // Handle not logged in
            }
        }
    }

    fun logout() {
        authManager.signOut()
    }
}

class ProfileViewModelFactory(
    private val usersManager: UsersManager,
    private val authManager: AuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(usersManager, authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
