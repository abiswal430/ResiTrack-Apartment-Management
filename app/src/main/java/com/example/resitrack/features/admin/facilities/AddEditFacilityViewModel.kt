package com.example.resitrack.features.admin.facilities

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.resitrack.data.manager.FacilitiesManager
import com.example.resitrack.data.model.Facility
import com.example.resitrack.data.model.TimeSlot
import com.example.resitrack.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddEditFacilityUiState(
    val facilityId: String = "",
    val name: String = "",
    val description: String = "",
    val bookingRequired: Boolean = true,
    val isAvailable: Boolean = true, // New state for availability
    val timeSlots: List<TimeSlot> = emptyList(),
    val screenTitle: String = "Add Facility",
    val buttonText: String = "Add Facility",
    val isLoading: Boolean = true,
    val saveResult: Resource<String>? = null
)

class AddEditFacilityViewModel(
    private val facilitiesManager: FacilitiesManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditFacilityUiState())
    val uiState = _uiState.asStateFlow()

    private val facilityId: String? = savedStateHandle["facilityId"]

    init {
        if (facilityId != null) {
            _uiState.value = _uiState.value.copy(screenTitle = "Edit Facility", buttonText = "Save Changes")
            loadFacilityDetails(facilityId)
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private fun loadFacilityDetails(id: String) {
        viewModelScope.launch {
            val facility = facilitiesManager.getFacilityDetails(id)
            if (facility != null) {
                _uiState.value = _uiState.value.copy(
                    facilityId = facility.id,
                    name = facility.name,
                    description = facility.description,
                    bookingRequired = facility.bookingRequired,
                    isAvailable = facility.isAvailable,
                    timeSlots = facility.timeSlots,
                    isLoading = false
                )
            }
        }
    }

    fun onNameChange(value: String) { _uiState.value = _uiState.value.copy(name = value) }
    fun onDescriptionChange(value: String) { _uiState.value = _uiState.value.copy(description = value) }
    fun onBookingRequiredChange(value: Boolean) { _uiState.value = _uiState.value.copy(bookingRequired = value) }
    fun onAvailabilityChange(value: Boolean) { _uiState.value = _uiState.value.copy(isAvailable = value) } // New function

    fun onAddTimeSlot(startTime: String, endTime: String) {
        val newSlot = TimeSlot(
            slotId = "${startTime.replace(":", "")}-${endTime.replace(":", "")}",
            startTime = startTime,
            endTime = endTime
        )
        _uiState.value = _uiState.value.copy(timeSlots = _uiState.value.timeSlots + newSlot)
    }

    fun onRemoveTimeSlot(timeSlot: TimeSlot) {
        _uiState.value = _uiState.value.copy(timeSlots = _uiState.value.timeSlots - timeSlot)
    }

    fun saveFacility() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(saveResult = Resource.Loading())
            try {
                val facility = Facility(
                    id = if (facilityId != null) facilityId else _uiState.value.name.lowercase().replace(" ", "-"),
                    name = _uiState.value.name,
                    description = _uiState.value.description,
                    bookingRequired = _uiState.value.bookingRequired,
                    isAvailable = _uiState.value.isAvailable, // Save new status
                    timeSlots = _uiState.value.timeSlots
                )

                if (facilityId != null) {
                    facilitiesManager.updateFacility(facility)
                    _uiState.value = _uiState.value.copy(saveResult = Resource.Success("Facility updated successfully!"))
                } else {
                    facilitiesManager.addFacility(facility)
                    _uiState.value = _uiState.value.copy(saveResult = Resource.Success("Facility added successfully!"))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(saveResult = Resource.Error(e.localizedMessage ?: "Operation failed"))
            }
        }
    }

    fun resetSaveResult() {
        _uiState.value = _uiState.value.copy(saveResult = null)
    }
}


class AddEditFacilityViewModelFactory(
    private val facilitiesManager: FacilitiesManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEditFacilityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddEditFacilityViewModel(facilitiesManager, savedStateHandle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

