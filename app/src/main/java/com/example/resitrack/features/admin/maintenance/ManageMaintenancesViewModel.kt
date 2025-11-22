package com.example.resitrack.features.admin.maintenance

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.resitrack.data.manager.MaintenanceManager
import com.example.resitrack.data.model.MaintenanceCycle
import com.example.resitrack.data.model.MaintenancePayment
import com.example.resitrack.util.Resource
import com.google.firebase.Timestamp
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
import java.util.Calendar
import java.util.Date

data class ManageMaintenanceUiState(
    val cycles: List<MaintenanceCycle> = emptyList(),
    val paymentsForSelectedCycle: List<MaintenancePayment> = emptyList(),
    val expandedCycleId: String? = null,
    val createCycleResult: Resource<String>? = null,
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class ManageMaintenanceViewModel(private val manager: MaintenanceManager) : ViewModel() {
    private val _uiState = MutableStateFlow(ManageMaintenanceUiState())
    val uiState: StateFlow<ManageMaintenanceUiState> = _uiState.asStateFlow()

    private val _selectedCycleId = MutableStateFlow<String?>(null)

    init {
        manager.getMaintenanceCyclesFlow()
            .onEach { cycles -> _uiState.update { it.copy(cycles = cycles, isLoading = false) } }
            .launchIn(viewModelScope)

        _selectedCycleId.flatMapLatest { cycleId ->
            if (cycleId == null) {
                flowOf(emptyList())
            } else {
                manager.getPaymentsForCycleFlow(cycleId)
            }
        }.onEach { payments ->
            _uiState.update { it.copy(paymentsForSelectedCycle = payments) }
        }.launchIn(viewModelScope)
    }

    fun onCycleSelected(cycleId: String) {
        val newId = if (_uiState.value.expandedCycleId == cycleId) null else cycleId
        _selectedCycleId.value = newId
        _uiState.update { it.copy(expandedCycleId = newId) }
    }

    @SuppressLint("DefaultLocale")
    fun createNewCycle(title: String, amount: Double, dueDate: Date) {
        viewModelScope.launch {
            _uiState.update { it.copy(createCycleResult = Resource.Loading()) }
            try {
                val calendar = Calendar.getInstance().apply { time = dueDate }
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1
                val cycleId = "${year}_${String.format("%02d", month)}"

                val cycle = MaintenanceCycle(
                    id = cycleId,
                    title = title,
                    amountDue = amount,
                    dueDate = Timestamp(dueDate),
                    year = year,
                    month = month
                )
                manager.createNewCycle(cycle)
                _uiState.update { it.copy(createCycleResult = Resource.Success("New cycle created!")) }
            } catch (e: Exception) {
                _uiState.update { it.copy(createCycleResult = Resource.Error(e.localizedMessage ?: "Failed")) }
            }
        }
    }

    fun updatePaymentStatus(paymentId: String, currentStatus: String) {
        viewModelScope.launch {
            val newStatus = if (currentStatus == "Paid") "Pending" else "Paid"
            manager.updatePaymentStatus(paymentId, newStatus)
        }
    }

    fun resetCreateResult() = _uiState.update { it.copy(createCycleResult = null) }
}

class ManageMaintenanceViewModelFactory(private val manager: MaintenanceManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManageMaintenanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ManageMaintenanceViewModel(manager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
