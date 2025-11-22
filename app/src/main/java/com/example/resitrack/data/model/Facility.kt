package com.example.resitrack.data.model

import com.google.firebase.firestore.DocumentId

data class TimeSlot(
    val slotId: String = "",
    val startTime: String = "",
    val endTime: String = ""
)

data class Facility(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val bookingRequired: Boolean = true,
    val timeSlots: List<TimeSlot> = emptyList(),
    val isAvailable: Boolean = true
)