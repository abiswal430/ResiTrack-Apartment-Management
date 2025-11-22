package com.example.resitrack.features.admin.notices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.resitrack.data.manager.NoticesManager
import com.example.resitrack.data.model.Notice
import com.example.resitrack.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManageNoticesUiState(
    val notices: List<Notice> = emptyList(),
    val searchQuery: String = "",
    val filteredNotices: List<Notice> = emptyList(),
    val noticeToDelete: Notice? = null,
    val deleteResult: Resource<String>? = null,
    val isLoading: Boolean = true
)

class ManageNoticesViewModel(private val noticesManager: NoticesManager) : ViewModel() {
    private val _uiState = MutableStateFlow(ManageNoticesUiState(isLoading = true))
    val uiState: StateFlow<ManageNoticesUiState> = _uiState.asStateFlow()

    init {
        noticesManager.getAllNoticesFlow()
            .onEach { noticeList ->
                _uiState.update { currentState ->
                    currentState.copy(
                        notices = noticeList,
                        filteredNotices = filterNotices(noticeList, currentState.searchQuery),
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                filteredNotices = filterNotices(currentState.notices, query)
            )
        }
    }

    fun onStartDelete(notice: Notice) {
        _uiState.update { it.copy(noticeToDelete = notice) }
    }

    fun onConfirmDelete() {
        viewModelScope.launch {
            _uiState.value.noticeToDelete?.let { notice ->
                try {
                    noticesManager.deleteNotice(notice.id)
                    _uiState.update { it.copy(noticeToDelete = null, deleteResult = Resource.Success("Notice deleted successfully!")) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(deleteResult = Resource.Error(e.localizedMessage ?: "Delete failed")) }
                }
            }
        }
    }

    fun onDismissDelete() {
        _uiState.update { it.copy(noticeToDelete = null) }
    }

    fun resetDeleteResult() {
        _uiState.update { it.copy(deleteResult = null) }
    }

    private fun filterNotices(notices: List<Notice>, query: String): List<Notice> {
        if (query.isBlank()) {
            return notices
        }
        return notices.filter {
            it.title.contains(query, ignoreCase = true) || it.message.contains(query, ignoreCase = true)
        }
    }
}

class ManageNoticesViewModelFactory(
    private val noticesManager: NoticesManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManageNoticesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ManageNoticesViewModel(noticesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

