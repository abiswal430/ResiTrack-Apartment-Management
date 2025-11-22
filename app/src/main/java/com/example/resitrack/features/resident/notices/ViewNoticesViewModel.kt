package com.example.resitrack.features.resident.notices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.resitrack.data.manager.NoticesManager
import com.example.resitrack.data.model.Notice
import kotlinx.coroutines.flow.*

data class ViewNoticesUiState(
    val notices: List<Notice> = emptyList(),
    val isLoading: Boolean = true
)

class ViewNoticesViewModel(noticesManager: NoticesManager) : ViewModel() {

    val uiState: StateFlow<ViewNoticesUiState> = noticesManager.getAllResidentNoticesFlow()
        .map { noticeList ->
            ViewNoticesUiState(notices = noticeList, isLoading = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ViewNoticesUiState(isLoading = true)
        )
}

class ViewNoticesViewModelFactory(
    private val noticesManager: NoticesManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewNoticesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ViewNoticesViewModel(noticesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
