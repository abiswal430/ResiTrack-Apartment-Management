package com.example.resitrack.features.admin.notices

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.resitrack.data.manager.NoticesManager
import com.example.resitrack.data.model.Notice
import com.example.resitrack.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddEditNoticeUiState(
    val title: String = "",
    val message: String = "",
    val screenTitle: String = "Post Notice",
    val buttonText: String = "Post Notice",
    val isLoading: Boolean = true,
    val isActive: Boolean = true,
    val saveResult: Resource<String>? = null
)

class AddEditNoticeViewModel(
    private val noticesManager: NoticesManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditNoticeUiState())
    val uiState = _uiState.asStateFlow()

    private val noticeId: String? = savedStateHandle["noticeId"]

    init {
        if (noticeId != null) {
            _uiState.value = _uiState.value.copy(
                screenTitle = "Edit Notice",
                buttonText = "Save Changes"
            )
            loadNoticeDetails(noticeId)
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private fun loadNoticeDetails(id: String) {
        viewModelScope.launch {
            val notice = noticesManager.getNoticeDetails(id)
            if (notice != null) {
                _uiState.value = _uiState.value.copy(
                    title = notice.title,
                    message = notice.message,
                    isLoading = false,
                    isActive = notice.isActive,
                )
            }
        }
    }

    fun onTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
    }

    fun onMessageChange(value: String) {
        _uiState.value = _uiState.value.copy(message = value)
    }

    fun onIsActiveChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(isActive = value)
    }

    fun saveNotice() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(saveResult = Resource.Loading())
            try {
                if (noticeId != null) {
                    val updatedNotice = Notice(
                        id = noticeId,
                        title = _uiState.value.title,
                        message = _uiState.value.message,
                        isActive = _uiState.value.isActive
                    )
                    noticesManager.updateNotice(updatedNotice)
                    _uiState.value =
                        _uiState.value.copy(saveResult = Resource.Success("Notice updated successfully!"))
                } else {
                    val newNotice = Notice(
                        title = _uiState.value.title,
                        message = _uiState.value.message,
                        isActive = true
                    )
                    noticesManager.addNotice(newNotice)
                    _uiState.value =
                        _uiState.value.copy(saveResult = Resource.Success("Notice posted successfully!"))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    saveResult = Resource.Error(
                        e.localizedMessage ?: "Operation failed"
                    )
                )
            }
        }
    }

    fun resetSaveResult() {
        _uiState.value = _uiState.value.copy(saveResult = null)
    }
}

class AddEditNoticeViewModelFactory(
    private val noticesManager: NoticesManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEditNoticeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddEditNoticeViewModel(noticesManager, savedStateHandle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
