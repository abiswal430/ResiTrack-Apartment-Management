package com.example.resitrack.features.resident.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.resitrack.data.manager.AuthManager
import com.example.resitrack.data.manager.NoticesManager
import com.example.resitrack.data.manager.UsersManager
import com.example.resitrack.data.model.Notice
import com.example.resitrack.data.model.User
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ResidentDashboardUiState(
    val user: User? = null,
    val recentActivity: List<Notice> = emptyList(), // Simplified to notices for now
    val isLoading: Boolean = true
)

class ResidentDashboardViewModel(
    private val usersManager: UsersManager,
    private val noticesManager: NoticesManager,
    private val authManager: AuthManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(ResidentDashboardUiState())
    val uiState: StateFlow<ResidentDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            val uid = authManager.getCurrentUserId()
            if (uid != null) {
                // Fetch user details once
                val userDetails = usersManager.getResidentDetails(uid)
                _uiState.update { it.copy(user = userDetails) }

                // Listen for recent activity in real-time
                noticesManager.getLatestNoticesFlow(limit = 3).collect { notices ->
                    _uiState.update {
                        it.copy(
                            recentActivity = notices,
                            isLoading = false
                        )
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false) } // Handle case where user is not logged in
            }
        }
    }
}

class ResidentDashboardViewModelFactory(
    private val usersManager: UsersManager,
    private val noticesManager: NoticesManager,
    private val authManager: AuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ResidentDashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ResidentDashboardViewModel(usersManager, noticesManager, authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

