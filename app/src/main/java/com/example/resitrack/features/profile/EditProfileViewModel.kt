package com.example.resitrack.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.resitrack.data.manager.AuthManager
import com.example.resitrack.data.manager.UsersManager
import com.example.resitrack.data.model.User
import com.example.resitrack.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val fullName: String = "",
    val contactNumber: String = "",
    val flatNo: String = "", // Added field for flat number
    val isLoading: Boolean = true,
    val saveResult: Resource<String>? = null,
    val currentUser: User? = null
)

class EditProfileViewModel(
    private val usersManager: UsersManager,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadInitialProfile()
    }

    private fun loadInitialProfile() {
        viewModelScope.launch {
            val userId = authManager.getCurrentUserId()
            if (userId != null) {
                val user = usersManager.getResidentDetails(userId)
                if (user != null) {
                    _uiState.update {
                        it.copy(
                            fullName = user.fullName,
                            contactNumber = user.contactNumber,
                            flatNo = user.flatNo, // Load flat number
                            currentUser = user,
                            isLoading = false
                        )
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onFullNameChange(value: String) = _uiState.update { it.copy(fullName = value) }
    fun onContactNumberChange(value: String) = _uiState.update { it.copy(contactNumber = value) }
    fun onFlatNoChange(value: String) = _uiState.update { it.copy(flatNo = value) } // New function

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(saveResult = Resource.Loading()) }
            val currentUser = _uiState.value.currentUser
            if (currentUser != null) {
                try {
                    // Create an updated user object
                    val updatedUser = currentUser.copy(
                        fullName = _uiState.value.fullName,
                        contactNumber = _uiState.value.contactNumber,
                        flatNo = _uiState.value.flatNo // Save updated flat number
                    )
                    usersManager.updateResident(updatedUser)
                    _uiState.update { it.copy(saveResult = Resource.Success("Profile updated successfully!")) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(saveResult = Resource.Error(e.localizedMessage ?: "Update failed")) }
                }
            } else {
                _uiState.update { it.copy(saveResult = Resource.Error("Could not find user to update.")) }
            }
        }
    }

    fun resetSaveResult() = _uiState.update { it.copy(saveResult = null) }
}

class EditProfileViewModelFactory(
    private val usersManager: UsersManager,
    private val authManager: AuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditProfileViewModel(usersManager, authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

