package com.example.resitrack.features.admin.maintenance

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.resitrack.data.manager.MaintenanceManager
import com.example.resitrack.data.model.MaintenanceCycle
import com.example.resitrack.util.Resource
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

data class AddEditCycleUiState(
    val title: String = "",
    val amount: String = "",
    val dueDate: Date? = null,
    val isEditMode: Boolean = false,
    val screenTitle: String = "Create Maintenance Cycle",
    val buttonText: String = "Create Cycle",
    val isLoading: Boolean = true,
    val saveResult: Resource<String>? = null
)

class AddEditMaintenanceCycleViewModel(
    private val manager: MaintenanceManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditCycleUiState())
    val uiState = _uiState.asStateFlow()
    private val cycleId: String? = savedStateHandle["cycleId"]

    init {
        if (cycleId != null) {
            _uiState.update {
                it.copy(
                    isEditMode = true,
                    screenTitle = "Edit Maintenance Cycle",
                    buttonText = "Save Changes"
                )
            }
            loadCycleDetails(cycleId)
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadCycleDetails(id: String) {
        viewModelScope.launch {
            val cycle = manager.getCycleDetails(id)
            if (cycle != null) {
                _uiState.update {
                    it.copy(
                        title = cycle.title,
                        amount = cycle.amountDue.toString(),
                        dueDate = cycle.dueDate?.toDate(),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value) }
    fun onAmountChange(value: String) = _uiState.update { it.copy(amount = value) }
    fun onDateChange(value: Date?) = _uiState.update { it.copy(dueDate = value) }

    fun saveCycle() {
        viewModelScope.launch {
            _uiState.update { it.copy(saveResult = Resource.Loading()) }
            try {
                val dueDate = _uiState.value.dueDate!!
                val calendar = Calendar.getInstance().apply { time = dueDate }
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1

                val newCycleId = if (_uiState.value.isEditMode) {
                    cycleId!!
                } else {
                    "${year}_${String.format("%02d", month)}"
                }

                val cycle = MaintenanceCycle(
                    id = newCycleId,
                    title = _uiState.value.title,
                    amountDue = _uiState.value.amount.toDoubleOrNull() ?: 0.0,
                    dueDate = Timestamp(dueDate),
                    year = year,
                    month = month
                )

                if (_uiState.value.isEditMode) {
                    manager.updateCycle(cycle)
                } else {
                    manager.createNewCycle(cycle)
                }
                _uiState.update { it.copy(saveResult = Resource.Success("Cycle saved successfully!")) }

            } catch (e: Exception) {
                _uiState.update { it.copy(saveResult = Resource.Error(e.localizedMessage ?: "Save failed")) }
            }
        }
    }

    fun resetSaveResult() = _uiState.update { it.copy(saveResult = null) }
}

class AddEditMaintenanceCycleViewModelFactory(
    private val manager: MaintenanceManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEditMaintenanceCycleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddEditMaintenanceCycleViewModel(manager, savedStateHandle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
