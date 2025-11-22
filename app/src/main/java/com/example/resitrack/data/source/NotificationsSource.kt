package com.example.resitrack.data.source

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NotificationsSource {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun saveToken(userId: String, token: String) {
        val tokenData = mapOf("token" to token)
        firestore.collection("fcmTokens").document(userId).set(tokenData).await()
    }
}
