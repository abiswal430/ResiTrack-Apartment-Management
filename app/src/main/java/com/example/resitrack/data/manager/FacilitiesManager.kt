package com.example.resitrack.data.manager

import com.example.resitrack.data.model.Facility
import com.example.resitrack.data.model.TimeSlot
import com.example.resitrack.data.source.FacilitiesSource
import java.util.Date

class FacilitiesManager(private val source: FacilitiesSource) {
    fun getAllFacilitiesFlow() = source.getAllFacilitiesFlow()
    suspend fun getFacilityDetails(facilityId: String) = source.getFacilityDetails(facilityId)
    fun getBookingDetailsFlow(facilityId: String, date: Date) = source.getBookingDetailsFlow(facilityId, date)
    suspend fun addFacility(facility: Facility) = source.addFacility(facility)
    suspend fun updateFacility(facility: Facility) = source.updateFacility(facility)
    suspend fun deleteFacility(facilityId: String) = source.deleteFacility(facilityId)

    suspend fun getFacilityBookingsFlow(facilityId: String, date: String) = source.getFacilityBookingsFlow(facilityId, date)
    fun getUserBookingsFlow(userId: String) = source.getUserBookingsFlow(userId)
    suspend fun bookSlot(
        userId: String,
        facility: Facility,
        date: String,
        timeSlot: TimeSlot
    ) = source.bookSlot(userId, facility, date, timeSlot)
}
