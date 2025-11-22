package com.example.resitrack.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.resitrack.data.manager.InvitationsManager
import com.example.resitrack.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistrationUiState(
    val invitationCode: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val saveResult: Resource<String>? = null
)

class RegistrationViewModel(private val manager: InvitationsManager) : ViewModel() {
    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState = _uiState.asStateFlow()

    fun onCodeChange(v: String) = _uiState.update { it.copy(invitationCode = v) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v) }
    fun onConfirmPasswordChange(v: String) = _uiState.update { it.copy(confirmPassword = v) }

    fun registerUser() {
        viewModelScope.launch {
            if (_uiState.value.password != _uiState.value.confirmPassword) {
                _uiState.update { it.copy(saveResult = Resource.Error("Passwords do not match.")) }
                return@launch
            }
            _uiState.update { it.copy(saveResult = Resource.Loading()) }
            try {
                manager.completeRegistration(
                    _uiState.value.invitationCode.trim(),
                    _uiState.value.email.trim(),
                    _uiState.value.password
                )
                _uiState.update { it.copy(saveResult = Resource.Success("Registration successful! Please login.")) }
            } catch (e: Exception) {
                _uiState.update { it.copy(saveResult = Resource.Error(e.localizedMessage ?: "Registration failed")) }
            }
        }
    }
    fun resetSaveResult() = _uiState.update { it.copy(saveResult = null) }
}

class RegistrationViewModelFactory(private val manager: InvitationsManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegistrationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegistrationViewModel(manager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
