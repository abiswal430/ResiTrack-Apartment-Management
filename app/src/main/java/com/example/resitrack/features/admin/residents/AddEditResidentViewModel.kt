package com.example.resitrack.features.admin.residents

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.resitrack.data.manager.InvitationsManager
import com.example.resitrack.data.manager.UsersManager
import com.example.resitrack.data.model.Invitation
import com.example.resitrack.data.model.User
import com.example.resitrack.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class AddEditResidentUiState(
    val fullName: String = "",
    val contactNumber: String = "",
    val email: String = "",
    val flatNo: String = "",
    val screenTitle: String = "Invite Resident",
    val buttonText: String = "Create Invitation",
    val isEditMode: Boolean = false,
    val isLoading: Boolean = true,
    val saveResult: Resource<String>? = null,
    val currentUser: User? = null // To hold original user data in edit mode
)

class AddEditResidentViewModel(
    private val usersManager: UsersManager,
    private val invitationsManager: InvitationsManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditResidentUiState())
    val uiState = _uiState.asStateFlow()

    private val residentId: String? = savedStateHandle["residentId"]

    init {
        if (residentId != null) {
            _uiState.update {
                it.copy(
                    isEditMode = true,
                    screenTitle = "Edit Resident",
                    buttonText = "Save Changes"
                )
            }
            loadResidentDetails(residentId)
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadResidentDetails(uid: String) {
        viewModelScope.launch {
            val user = usersManager.getResidentDetails(uid)
            if (user != null) {
                _uiState.update {
                    it.copy(
                        fullName = user.fullName,
                        contactNumber = user.contactNumber,
                        email = user.email,
                        flatNo = user.flatNo,
                        currentUser = user,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onFullNameChange(value: String) = _uiState.update { it.copy(fullName = value) }
    fun onContactChange(value: String) = _uiState.update { it.copy(contactNumber = value) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value) }
    fun onFlatNoChange(value: String) = _uiState.update { it.copy(flatNo = value) }

    fun onSaveClick() {
        if (_uiState.value.isEditMode) {
            updateResident()
        } else {
            createInvitation()
        }
    }

    private fun updateResident() {
        viewModelScope.launch {
            _uiState.update { it.copy(saveResult = Resource.Loading()) }
            val currentUser = _uiState.value.currentUser
            if (currentUser != null) {
                try {
                    val updatedUser = currentUser.copy(
                        fullName = _uiState.value.fullName,
                        contactNumber = _uiState.value.contactNumber,
                        flatNo = _uiState.value.flatNo
                    )
                    usersManager.updateResident(updatedUser)
                    _uiState.update { it.copy(saveResult = Resource.Success("Resident updated successfully!")) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(saveResult = Resource.Error(e.localizedMessage ?: "Update failed")) }
                }
            }
        }
    }

    private fun createInvitation() {
        viewModelScope.launch {
            _uiState.update { it.copy(saveResult = Resource.Loading()) }
            try {
                val newInvitation = Invitation(
                    invitationCode = UUID.randomUUID().toString().substring(0, 6).uppercase(),
                    fullName = _uiState.value.fullName,
                    contactNumber = _uiState.value.contactNumber,
                    email = _uiState.value.email,
                    flatNo = _uiState.value.flatNo
                )
                invitationsManager.createInvitation(newInvitation)
                _uiState.update { it.copy(saveResult = Resource.Success("Invitation created: ${newInvitation.invitationCode}")) }
            } catch (e: Exception) {
                _uiState.update { it.copy(saveResult = Resource.Error(e.localizedMessage ?: "Failed")) }
            }
        }
    }

    fun resetSaveResult() = _uiState.update { it.copy(saveResult = null) }
}

class AddEditResidentViewModelFactory(
    private val usersManager: UsersManager,
    private val invitationsManager: InvitationsManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEditResidentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddEditResidentViewModel(usersManager, invitationsManager, savedStateHandle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

