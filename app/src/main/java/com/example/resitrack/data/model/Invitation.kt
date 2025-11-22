package com.example.resitrack.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Invitation(
    @DocumentId val id: String = "",
    val invitationCode: String = "",
    val email: String = "",
    val fullName: String = "",
    val contactNumber: String = "",
    val flatNo: String = "",
    val status: String = "Pending", // Can be "Pending" or "Completed"
    @ServerTimestamp val createdAt: Timestamp? = null
)