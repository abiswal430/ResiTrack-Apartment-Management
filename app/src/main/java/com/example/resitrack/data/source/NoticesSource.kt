package com.example.resitrack.data.source

import com.example.resitrack.data.model.Notice
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NoticesSource {
    private val firestore = FirebaseFirestore.getInstance()

    fun getAllNoticesFlow(): Flow<List<Notice>> = callbackFlow {
        val listenerRegistration = firestore.collection("notices")
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
        awaitClose { listenerRegistration.remove() }
    }

    fun getAllResidentNoticesFlow(): Flow<List<Notice>> = callbackFlow {
        val listenerRegistration = firestore.collection("notices")
            .whereEqualTo("isActive", true)
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
        awaitClose { listenerRegistration.remove() }
    }
    fun getLatestNoticesFlow(limit: Long): Flow<List<Notice>> = callbackFlow {
        val listener = firestore.collection("notices")
            .whereEqualTo("isActive", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                snapshot?.let { trySend(it.toObjects()) }
            }
        awaitClose { listener.remove() }
    }

    suspend fun getNoticeDetails(noticeId: String): Notice? {
        return firestore.collection("notices").document(noticeId).get().await().toObject<Notice>()
    }

    suspend fun addNotice(notice: Notice) {
        firestore.collection("notices").add(notice).await()
    }

    suspend fun updateNotice(notice: Notice) {
        firestore.collection("notices").document(notice.id).set(notice).await()
    }

    suspend fun deleteNotice(noticeId: String) {
        firestore.collection("notices").document(noticeId).delete().await()
    }
}
