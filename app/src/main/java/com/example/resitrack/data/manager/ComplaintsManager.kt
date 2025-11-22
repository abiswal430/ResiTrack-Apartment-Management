package com.example.resitrack.data.manager

import com.example.resitrack.data.model.Complaint
import com.example.resitrack.data.source.ComplaintsSource

class ComplaintsManager(private val source: ComplaintsSource) {
    fun getPendingComplaintsCountFlow() = source.getPendingComplaintsCountFlow()
    fun getResolvedComplaintsCountFlow() = source.getResolvedComplaintsCountFlow()
    fun getRecentComplaintsFlow(limit: Long) = source.getRecentComplaintsFlow(limit)
    fun getAllComplaintsFlow() = source.getAllComplaintsFlow()
    fun getComplaintsForUserFlow(userId: String) = source.getComplaintsForUserFlow(userId)
    fun getComplaintDetailsFlow(complaintId: String) = source.getComplaintDetailsFlow(complaintId)
    suspend fun updateComplaint(complaint: Complaint) = source.updateComplaint(complaint)

    suspend fun addComplaint(complaint: Complaint) = source.addComplaint(complaint)
}

