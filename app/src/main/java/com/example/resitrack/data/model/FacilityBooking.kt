package com.example.resitrack.data.model

import com.google.firebase.firestore.DocumentId

data class FacilityBooking(
    @DocumentId val id: String = "",
    val facilityId: String = "",
    val date: String = "",
    val bookedSlots: Map<String, String> = emptyMap()
)