package com.example.resitrack.data.manager

import com.example.resitrack.data.model.MaintenanceCycle
import com.example.resitrack.data.source.MaintenanceSource
import com.example.resitrack.data.source.UsersSource

class MaintenanceManager(
    private val maintenanceSource: MaintenanceSource,
    private val usersSource: UsersSource // Also needs UsersSource to get resident list
) {
    fun getMaintenanceCyclesFlow() = maintenanceSource.getMaintenanceCyclesFlow()
    fun getPaymentsForCycleFlow(cycleId: String) = maintenanceSource.getPaymentsForCycleFlow(cycleId)
    suspend fun updatePaymentStatus(paymentId: String, newStatus: String) = maintenanceSource.updatePaymentStatus(paymentId, newStatus)
    suspend fun getCycleDetails(cycleId: String) = maintenanceSource.getCycleDetails(cycleId)
    suspend fun updateCycle(cycle: MaintenanceCycle) = maintenanceSource.updateCycle(cycle)

    suspend fun createNewCycle(cycle: MaintenanceCycle) {
        val residents = usersSource.getAllResidentsOneShot()
        maintenanceSource.createNewCycle(cycle, residents)
    }
}

