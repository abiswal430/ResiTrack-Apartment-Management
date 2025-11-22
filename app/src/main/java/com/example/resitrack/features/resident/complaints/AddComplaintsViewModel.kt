package com.example.resitrack.features.resident.complaints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.resitrack.data.manager.AuthManager
import com.example.resitrack.data.manager.ComplaintsManager
import com.example.resitrack.data.manager.UsersManager
import com.example.resitrack.data.model.Complaint
import com.example.resitrack.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddComplaintUiState(
    val subject: String = "",
    val description: String = "",
    val saveResult: Resource<String>? = null
)

class AddComplaintViewModel(
    private val complaintsManager: ComplaintsManager,
    private val usersManager: UsersManager,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddComplaintUiState())
    val uiState = _uiState.asStateFlow()

    fun onSubjectChange(value: String) = _uiState.update { it.copy(subject = value) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }

    fun fileComplaint() {
        viewModelScope.launch {
            _uiState.update { it.copy(saveResult = Resource.Loading()) }
            val userId = authManager.getCurrentUserId()
            val userDetails = userId?.let { usersManager.getResidentDetails(it) }

            if (userId == null || userDetails == null) {
                _uiState.update { it.copy(saveResult = Resource.Error("Could not identify user.")) }
                return@launch
            }

            try {
                val newComplaint = Complaint(
                    subject = _uiState.value.subject,
                    description = _uiState.value.description,
                    raisedByUid = userId,
                    raisedByName = userDetails.fullName,
                    raisedByFlatNo = userDetails.flatNo,
                    status = "Pending"
                )
                complaintsManager.addComplaint(newComplaint)
                _uiState.update { it.copy(saveResult = Resource.Success("Complaint filed successfully!")) }
            } catch (e: Exception) {
                _uiState.update { it.copy(saveResult = Resource.Error(e.localizedMessage ?: "Failed to file complaint")) }
            }
        }
    }

    fun resetSaveResult() = _uiState.update { it.copy(saveResult = null) }
}

class AddComplaintViewModelFactory(
    private val complaintsManager: ComplaintsManager,
    private val usersManager: UsersManager,
    private val authManager: AuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddComplaintViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddComplaintViewModel(complaintsManager, usersManager, authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
