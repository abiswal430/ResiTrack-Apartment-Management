package com.example.resitrack.features.admin.residents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.resitrack.data.manager.UsersManager
import com.example.resitrack.data.model.User
import com.example.resitrack.util.Resource
import com.google.firebase.Firebase
import com.google.firebase.functions.functions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ManageResidentsUiState(
    val residents: List<User> = emptyList(),
    val searchQuery: String = "",
    val filteredResidents: List<User> = emptyList(),
    val residentToDelete: User? = null, // Track which resident to delete
    val deleteResult: Resource<String>? = null,
    val isLoading: Boolean = true
)

class ManageResidentsViewModel(usersManager: UsersManager) : ViewModel() {
    private val _uiState = MutableStateFlow(ManageResidentsUiState(isLoading = true))
    val uiState: StateFlow<ManageResidentsUiState> = _uiState.asStateFlow()

    init {
        usersManager.getAllResidentsFlow()
            .onEach { residentList ->
                _uiState.update { currentState ->
                    currentState.copy(
                        residents = residentList,
                        filteredResidents = filterResidents(residentList, currentState.searchQuery),
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
                filteredResidents = filterResidents(currentState.residents, query)
            )
        }
    }

    fun onStartDelete(resident: User) {
        _uiState.update { it.copy(residentToDelete = resident) }
    }

    fun onConfirmDelete() {
        viewModelScope.launch {
            _uiState.value.residentToDelete?.let { resident ->
                try {
                    val data = hashMapOf("uid" to resident.uid)
                    Firebase.functions.getHttpsCallable("deleteUser").call(data).await()
                    _uiState.update { it.copy(residentToDelete = null, deleteResult = Resource.Success("Resident deleted!")) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(deleteResult = Resource.Error(e.localizedMessage ?: "Delete failed")) }
                }
            }
        }
    }

    fun onDismissDelete() {
        _uiState.update { it.copy(residentToDelete = null) }
    }

    fun resetDeleteResult() {
        _uiState.update { it.copy(deleteResult = null) }
    }

    private fun filterResidents(residents: List<User>, query: String): List<User> {
        if (query.isBlank()) return residents
        return residents.filter {
            it.fullName.contains(query, ignoreCase = true) || it.flatNo.contains(query, ignoreCase = true)
        }
    }
}
