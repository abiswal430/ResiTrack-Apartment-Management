package com.example.resitrack.features.admin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.resitrack.data.manager.ComplaintsManager
import com.example.resitrack.data.manager.UsersManager
import com.example.resitrack.data.model.Complaint
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val totalResidents: Int = 0,
    val pendingComplaints: Int = 0,
    val resolvedComplaints: Int = 0,
    val recentComplaints: List<Complaint> = emptyList(),
    val isLoading: Boolean = true
)

class AdminDashboardViewModel(
    usersManager: UsersManager,
    complaintsManager: ComplaintsManager
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        usersManager.getResidentsCountFlow(),
        complaintsManager.getPendingComplaintsCountFlow(),
        complaintsManager.getResolvedComplaintsCountFlow(),
        complaintsManager.getRecentComplaintsFlow(limit = 5)
    ) { residentsCount, pendingCount, resolvedCount, recentComplaints ->
        DashboardUiState(
            totalResidents = residentsCount,
            pendingComplaints = pendingCount,
            resolvedComplaints = resolvedCount,
            recentComplaints = recentComplaints,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )
}

class AdminDashboardViewModelFactory(
    private val usersManager: UsersManager,
    private val complaintsManager: ComplaintsManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminDashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminDashboardViewModel(usersManager, complaintsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

