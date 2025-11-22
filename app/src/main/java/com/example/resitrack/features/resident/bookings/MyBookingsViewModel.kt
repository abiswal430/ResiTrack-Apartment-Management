package com.example.resitrack.features.resident.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.resitrack.data.manager.AuthManager
import com.example.resitrack.data.manager.FacilitiesManager
import com.example.resitrack.data.model.UserBooking
import kotlinx.coroutines.flow.*

data class MyBookingsUiState(
    val bookings: List<UserBooking> = emptyList(),
    val isLoading: Boolean = true
)

class MyBookingsViewModel(
    facilitiesManager: FacilitiesManager,
    authManager: AuthManager
) : ViewModel() {

    val uiState: StateFlow<MyBookingsUiState> = flow {
        val userId = authManager.getCurrentUserId()
        if (userId != null) {
            facilitiesManager.getUserBookingsFlow(userId).collect { bookings ->
                emit(MyBookingsUiState(bookings = bookings, isLoading = false))
            }
        } else {
            emit(MyBookingsUiState(isLoading = false))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MyBookingsUiState(isLoading = true)
    )
}

class MyBookingsViewModelFactory(
    private val facilitiesManager: FacilitiesManager,
    private val authManager: AuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyBookingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyBookingsViewModel(facilitiesManager, authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
