package com.example.resitrack.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Complaint(
    @DocumentId val id: String = "",
    val subject: String = "",
    val description: String = "",
    val raisedByUid: String = "",
    val raisedByName: String = "",
    val raisedByFlatNo: String = "",
    val status: String = "Pending",
    @ServerTimestamp val createdAt: Timestamp? = null,
    val resolvedAt: Timestamp? = null
)