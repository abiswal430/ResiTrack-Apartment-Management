package com.example.resitrack.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class UserBooking(
    @DocumentId val id: String = "",
    val facilityId: String = "",
    val facilityName: String = "",
    val date: String = "",
    val slotId: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val bookedAt: Timestamp? = null
)
