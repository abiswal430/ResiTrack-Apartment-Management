package com.example.resitrack.features.admin.complaints

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.resitrack.data.manager.ComplaintsManager
import com.example.resitrack.data.model.Complaint
import com.example.resitrack.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ComplaintDetailsUiState(
    val complaint: Complaint? = null,
    val adminResponse: String = "",
    val newStatus: String = "",
    val isLoading: Boolean = true,
    val updateResult: Resource<String>? = null
)

class ComplaintDetailsViewModel(
    private val complaintsManager: ComplaintsManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val complaintId: String = savedStateHandle.get<String>("complaintId")!!
    private val _uiState = MutableStateFlow(ComplaintDetailsUiState())
    val uiState: StateFlow<ComplaintDetailsUiState> = _uiState.asStateFlow()

    init {
        complaintsManager.getComplaintDetailsFlow(complaintId)
            .onEach { complaint ->
                _uiState.update {
                    it.copy(
                        complaint = complaint,
                        newStatus = complaint?.status ?: "",
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onResponseChange(response: String) {
        _uiState.update { it.copy(adminResponse = response) }
    }

    fun onStatusChange(status: String) {
        _uiState.update { it.copy(newStatus = status) }
    }

    fun updateComplaint() {
        viewModelScope.launch {
            _uiState.update { it.copy(updateResult = Resource.Loading()) }
            val currentComplaint = _uiState.value.complaint
            if (currentComplaint != null) {
                try {
                    // This is a simplified response model. In a real app, you'd have a list of responses.
                    val updatedComplaint = currentComplaint.copy(
                        status = _uiState.value.newStatus,
                        description = "${currentComplaint.description}\n\nAdmin Response: ${_uiState.value.adminResponse}"
                    )
                    complaintsManager.updateComplaint(updatedComplaint)
                    _uiState.update { it.copy(updateResult = Resource.Success("Complaint updated!")) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(updateResult = Resource.Error(e.localizedMessage ?: "Update failed")) }
                }
            }
        }
    }

    fun resetUpdateResult() {
        _uiState.update { it.copy(updateResult = null) }
    }
}

class ComplaintDetailsViewModelFactory(
    private val complaintsManager: ComplaintsManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ComplaintDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ComplaintDetailsViewModel(complaintsManager, savedStateHandle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
