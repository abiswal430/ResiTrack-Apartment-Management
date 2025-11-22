package com.example.resitrack.data.model

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId val uid: String = "",
    val role: String = "resident",
    val fullName: String = "",
    val contactNumber: String = "",
    val email: String = "",
    val flatNo: String = ""
)