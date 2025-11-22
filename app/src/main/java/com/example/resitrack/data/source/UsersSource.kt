package com.example.resitrack.data.source

import com.example.resitrack.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UsersSource {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getUserRole(uid: String): String? {
        return firestore.collection("users").document(uid).get().await()
            .toObject(User::class.java)?.role
    }

    fun getResidentsCountFlow(): Flow<Int> = callbackFlow {
        val listenerRegistration = firestore.collection("users")
            .whereEqualTo("role", "resident")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.size())
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    fun getAllResidentsFlow(): Flow<List<User>> = callbackFlow {
        val listenerRegistration = firestore.collection("users")
            .whereEqualTo("role", "resident")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects())
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    suspend fun getResidentDetails(uid: String): User? {
        return firestore.collection("users").document(uid).get().await().toObject<User>()
    }

    suspend fun updateResident(user: User) {
        firestore.collection("users").document(user.uid).set(user).await()
    }

    suspend fun getAllResidentsOneShot(): List<User> {
        return firestore.collection("users")
            .whereEqualTo("role", "resident")
            .get()
            .await()
            .toObjects()
    }
}

