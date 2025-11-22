package com.example.resitrack.features.resident.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.resitrack.data.manager.AuthManager
import com.example.resitrack.data.manager.FacilitiesManager
import com.example.resitrack.data.model.Facility
import com.example.resitrack.data.model.TimeSlot
import com.example.resitrack.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class BookFacilityUiState(
    val facilities: List<Facility> = emptyList(),
    val selectedFacility: Facility? = null,
    val selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val availableTimeSlots: List<TimeSlot> = emptyList(),
    val bookedTimeSlots: List<String> = emptyList(),
    val selectedTimeSlot: TimeSlot? = null,
    val bookingResult: Resource<String>? = null,
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class BookFacilityViewModel(
    private val facilitiesManager: FacilitiesManager,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookFacilityUiState())
    val uiState: StateFlow<BookFacilityUiState> = _uiState.asStateFlow()

    private val _selection = MutableStateFlow(Pair<Facility?, String>(_uiState.value.selectedFacility, _uiState.value.selectedDate))

    init {
        facilitiesManager.getAllFacilitiesFlow()
            .onEach { facilities ->
                val availableFacilities = facilities.filter { it.isAvailable && it.bookingRequired }
                _uiState.update {
                    it.copy(
                        facilities = availableFacilities,
                        isLoading = false
                    )
                }
                if (availableFacilities.isNotEmpty() && _uiState.value.selectedFacility == null) {
                    onFacilitySelected(availableFacilities.first())
                }
            }.launchIn(viewModelScope)

        _selection.flatMapLatest { (facility, date) ->
            if (facility == null) {
                flowOf(emptyList<String>())
            } else {
                facilitiesManager.getFacilityBookingsFlow(facility.id, date)
                    .map { it?.bookedSlots?.keys?.toList() ?: emptyList() }
            }
        }.onEach { bookedSlots ->
            _uiState.update {
                it.copy(
                    bookedTimeSlots = bookedSlots,
                    availableTimeSlots = it.selectedFacility?.timeSlots ?: emptyList()
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onFacilitySelected(facility: Facility) {
        _uiState.update { it.copy(selectedFacility = facility, selectedTimeSlot = null) }
        _selection.value = Pair(facility, _uiState.value.selectedDate)
    }

    fun onDateSelected(dateMillis: Long) {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = dateMillis }
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        _uiState.update { it.copy(selectedDate = dateString, selectedTimeSlot = null) }
        _selection.value = Pair(_uiState.value.selectedFacility, dateString)
    }

    fun onTimeSlotSelected(timeSlot: TimeSlot) {
        _uiState.update { it.copy(selectedTimeSlot = timeSlot) }
    }

    fun bookFacility() {
        viewModelScope.launch {
            _uiState.update { it.copy(bookingResult = Resource.Loading()) }
            val userId = authManager.getCurrentUserId()
            val facility = _uiState.value.selectedFacility
            val timeSlot = _uiState.value.selectedTimeSlot
            val date = _uiState.value.selectedDate

            if (userId == null || facility == null || timeSlot == null) {
                _uiState.update { it.copy(bookingResult = Resource.Error("Please select all details.")) }
                return@launch
            }
            try {
                facilitiesManager.bookSlot(userId, facility, date, timeSlot)
                _uiState.update { it.copy(bookingResult = Resource.Success("Booking successful!")) }
            } catch (e: Exception) {
                _uiState.update { it.copy(bookingResult = Resource.Error(e.localizedMessage ?: "Booking failed")) }
            }
        }
    }

    fun resetBookingResult() = _uiState.update { it.copy(bookingResult = null) }
}


class BookFacilityViewModelFactory(
    private val facilitiesManager: FacilitiesManager,
    private val authManager: AuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookFacilityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookFacilityViewModel(facilitiesManager, authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
