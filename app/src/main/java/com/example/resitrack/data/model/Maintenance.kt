package com.example.resitrack.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class MaintenanceCycle(
    @DocumentId val id: String = "",
    val title: String = "",
    val amountDue: Double = 0.0,
    val dueDate: Timestamp? = null,
    val year: Int = 0,
    val month: Int = 0
)

data class MaintenancePayment(
    @DocumentId val id: String = "",
    val cycleId: String = "",
    val residentUid: String = "",
    val residentName: String = "",
    val flatNo: String = "",
    val status: String = "Pending",
    val paidOn: Timestamp? = null
)