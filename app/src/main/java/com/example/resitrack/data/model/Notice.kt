package com.example.resitrack.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

data class Notice(
    @DocumentId val id: String = "",
    val title: String = "",
    val message: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @get:PropertyName("isActive")
    val isActive: Boolean = true
)