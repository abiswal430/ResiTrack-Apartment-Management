package com.example.resitrack.data.source

import com.example.resitrack.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthSource {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    suspend fun signIn(email: String, password: String): String {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw Exception("Login failed: User not found")
    }

    suspend fun signUp(email: String, password: String, user: User): String {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: throw Exception("Registration failed: User UID not found")

        // Store user details in Firestore
        val userWithUid = user.copy(uid = uid) // Ensure UID is set
        firestore.collection("users").document(uid).set(userWithUid).await()

        return uid
    }

    // Function to get current user's UID
    fun getCurrentUserUid(): String? {
        return firebaseAuth.currentUser?.uid
    }

    // Function to get user role
    suspend fun getUserRole(uid: String): String? {
        return firestore.collection("users").document(uid).get().await()
            .toObject(User::class.java)?.role
    }

    // Function to fetch user details
    suspend fun getUserDetails(uid: String): User? {
        return firestore.collection("users").document(uid).get().await()
            .toObject(User::class.java)
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}