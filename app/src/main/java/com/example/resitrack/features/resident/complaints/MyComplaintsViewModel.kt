package com.example.resitrack.features.resident.complaints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.resitrack.data.manager.AuthManager
import com.example.resitrack.data.manager.ComplaintsManager
import com.example.resitrack.data.model.Complaint
import kotlinx.coroutines.flow.*

data class MyComplaintsUiState(
    val complaints: List<Complaint> = emptyList(),
    val isLoading: Boolean = true
)

class MyComplaintsViewModel(
    complaintsManager: ComplaintsManager,
    authManager: AuthManager
) : ViewModel() {

    val uiState: StateFlow<MyComplaintsUiState> = flow {
        val userId = authManager.getCurrentUserId()
        if (userId != null) {
            complaintsManager.getComplaintsForUserFlow(userId).collect { complaints ->
                emit(MyComplaintsUiState(complaints = complaints, isLoading = false))
            }
        } else {
            emit(MyComplaintsUiState(isLoading = false)) // Handle not logged in case
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MyComplaintsUiState(isLoading = true)
    )
}

class MyComplaintsViewModelFactory(
    private val complaintsManager: ComplaintsManager,
    private val authManager: AuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyComplaintsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyComplaintsViewModel(complaintsManager, authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
