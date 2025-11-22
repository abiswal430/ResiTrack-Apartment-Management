package com.example.resitrack.features.admin.facilities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.resitrack.data.manager.FacilitiesManager
import com.example.resitrack.data.model.Facility
import com.example.resitrack.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// UPDATED: UI state now includes bottom sheet information
data class ManageFacilitiesUiState(
    val facilities: List<Facility> = emptyList(),
    val searchQuery: String = "",
    val filteredFacilities: List<Facility> = emptyList(),
    val facilityToDelete: Facility? = null,
    val deleteResult: Resource<String>? = null,
    val selectedFacilityForSheet: Facility? = null, // For the bottom sheet
    val bookingsForSelectedFacility: Map<String, String> = emptyMap(), // Bookings for the sheet
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class ManageFacilitiesViewModel(private val facilitiesManager: FacilitiesManager) : ViewModel() {
    private val _uiState = MutableStateFlow(ManageFacilitiesUiState(isLoading = true))
    val uiState: StateFlow<ManageFacilitiesUiState> = _uiState.asStateFlow()

    // A flow to track the currently selected facility for the bottom sheet
    private val _selectedFacilityId = MutableStateFlow<String?>(null)

    init {
        // Fetch the list of all facilities
        facilitiesManager.getAllFacilitiesFlow()
            .onEach { facilityList ->
                _uiState.update { currentState ->
                    currentState.copy(
                        facilities = facilityList,
                        filteredFacilities = filterFacilities(facilityList, currentState.searchQuery),
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)

        // Reactively fetch bookings whenever the selected facility changes
        _selectedFacilityId.flatMapLatest { facilityId ->
            if (facilityId == null) {
                flowOf(null) // Emit null when no facility is selected
            } else {
                val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                facilitiesManager.getFacilityBookingsFlow(facilityId, today)
            }
        }.onEach { booking ->
            _uiState.update { it.copy(bookingsForSelectedFacility = booking?.bookedSlots ?: emptyMap()) }
        }.launchIn(viewModelScope)
    }

    // --- List Management ---
    fun onSearchQueryChange(query: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                filteredFacilities = filterFacilities(currentState.facilities, query)
            )
        }
    }

    // --- Delete Management ---
    fun onStartDelete(facility: Facility) = _uiState.update { it.copy(facilityToDelete = facility) }
    fun onConfirmDelete() {
        viewModelScope.launch {
            _uiState.value.facilityToDelete?.let { facility ->
                try {
                    facilitiesManager.deleteFacility(facility.id)
                    _uiState.update { it.copy(facilityToDelete = null, deleteResult = Resource.Success("Facility deleted!")) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(deleteResult = Resource.Error(e.localizedMessage ?: "Delete failed")) }
                }
            }
        }
    }
    fun onDismissDelete() = _uiState.update { it.copy(facilityToDelete = null) }
    fun resetDeleteResult() = _uiState.update { it.copy(deleteResult = null) }

    // --- Bottom Sheet Management ---
    fun onShowBottomSheet(facility: Facility) {
        _selectedFacilityId.value = facility.id
        _uiState.update { it.copy(selectedFacilityForSheet = facility) }
    }

    fun onDismissBottomSheet() {
        _selectedFacilityId.value = null
        _uiState.update { it.copy(selectedFacilityForSheet = null) }
    }

    private fun filterFacilities(facilities: List<Facility>, query: String): List<Facility> {
        if (query.isBlank()) return facilities
        return facilities.filter { it.name.contains(query, ignoreCase = true) }
    }
}

class ManageFacilitiesViewModelFactory(
    private val facilitiesManager: FacilitiesManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManageFacilitiesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ManageFacilitiesViewModel(facilitiesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

