package com.example.resitrack.data.source

import com.example.resitrack.data.model.Complaint
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ComplaintsSource {
    private val firestore = FirebaseFirestore.getInstance()

    fun getPendingComplaintsCountFlow(): Flow<Int> = callbackFlow {
        val listener = firestore.collection("complaints")
            .whereEqualTo("status", "Pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.size())
                }
            }
        awaitClose { listener.remove() }
    }

    fun getResolvedComplaintsCountFlow(): Flow<Int> = callbackFlow {
        val listener = firestore.collection("complaints")
            .whereEqualTo("status", "Resolved")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.size())
                }
            }
        awaitClose { listener.remove() }
    }

    fun getRecentComplaintsFlow(limit: Long): Flow<List<Complaint>> = callbackFlow {
        val listener = firestore.collection("complaints")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects())
                }
            }
        awaitClose { listener.remove() }
    }

    // NEW: Get a real-time flow of all complaints
    fun getAllComplaintsFlow(): Flow<List<Complaint>> = callbackFlow {
        val listener = firestore.collection("complaints")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects())
                }
            }
        awaitClose { listener.remove() }
    }

    // Get a real-time flow for a single complaint's details
    fun getComplaintDetailsFlow(complaintId: String): Flow<Complaint?> = callbackFlow {
        val listener = firestore.collection("complaints").document(complaintId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.toObject<Complaint>())
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    // Get a real-time flow of complaints for a specific user
    fun getComplaintsForUserFlow(userId: String): Flow<List<Complaint>> = callbackFlow {
        val listener = firestore.collection("complaints")
            .whereEqualTo("raisedByUid", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                snapshot?.let { trySend(it.toObjects()) }
            }
        awaitClose { listener.remove() }
    }

    // Add a new complaint document
    suspend fun addComplaint(complaint: Complaint) {
        firestore.collection("complaints").add(complaint).await()
    }

    // Update a complaint document
    suspend fun updateComplaint(complaint: Complaint) {
        firestore.collection("complaints").document(complaint.id).set(complaint).await()
    }
}

