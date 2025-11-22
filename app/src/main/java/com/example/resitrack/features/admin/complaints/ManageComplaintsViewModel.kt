package com.example.resitrack.features.admin.complaints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.resitrack.data.manager.ComplaintsManager
import com.example.resitrack.data.model.Complaint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class ManageComplaintsUiState(
    val complaints: List<Complaint> = emptyList(),
    val searchQuery: String = "",
    val statusFilter: String = "All", // "All", "Pending", "InProgress", "Resolved"
    val filteredComplaints: List<Complaint> = emptyList(),
    val isLoading: Boolean = true
)

class ManageComplaintsViewModel(complaintsManager: ComplaintsManager) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageComplaintsUiState(isLoading = true))
    val uiState: StateFlow<ManageComplaintsUiState> = _uiState.asStateFlow()

    init {
        complaintsManager.getAllComplaintsFlow()
            .onEach { complaintList ->
                _uiState.update { currentState ->
                    currentState.copy(
                        complaints = complaintList,
                        filteredComplaints = filterComplaints(complaintList, currentState.searchQuery, currentState.statusFilter),
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
                filteredComplaints = filterComplaints(currentState.complaints, query, currentState.statusFilter)
            )
        }
    }

    fun onStatusFilterChange(status: String) {
        _uiState.update { currentState ->
            currentState.copy(
                statusFilter = status,
                filteredComplaints = filterComplaints(currentState.complaints, currentState.searchQuery, status)
            )
        }
    }

    private fun filterComplaints(complaints: List<Complaint>, query: String, status: String): List<Complaint> {
        val statusFiltered = if (status == "All") {
            complaints
        } else {
            complaints.filter { it.status.equals(status, ignoreCase = true) }
        }

        if (query.isBlank()) {
            return statusFiltered
        }

        return statusFiltered.filter {
            it.raisedByName.contains(query, ignoreCase = true) ||
                    it.subject.contains(query, ignoreCase = true)
        }
    }
}

class ManageComplaintsViewModelFactory(
    private val complaintsManager: ComplaintsManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManageComplaintsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ManageComplaintsViewModel(complaintsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
