package com.example.resitrack.data.source

import com.example.resitrack.data.model.MaintenanceCycle
import com.example.resitrack.data.model.MaintenancePayment
import com.example.resitrack.data.model.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MaintenanceSource {
    private val firestore = FirebaseFirestore.getInstance()

    fun getMaintenanceCyclesFlow(): Flow<List<MaintenanceCycle>> = callbackFlow {
        val listener = firestore.collection("maintenanceCycles")
            .orderBy("year", Query.Direction.DESCENDING)
            .orderBy("month", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                snapshot?.let { trySend(it.toObjects()) }
            }
        awaitClose { listener.remove() }
    }

    fun getPaymentsForCycleFlow(cycleId: String): Flow<List<MaintenancePayment>> = callbackFlow {
        val listener = firestore.collection("maintenancePayments")
            .whereEqualTo("cycleId", cycleId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                snapshot?.let { trySend(it.toObjects()) }
            }
        awaitClose { listener.remove() }
    }

    suspend fun createNewCycle(cycle: MaintenanceCycle, residents: List<User>) {
        val batch = firestore.batch()
        val cycleRef = firestore.collection("maintenanceCycles").document(cycle.id)
        batch.set(cycleRef, cycle)
        residents.forEach { resident ->
            val paymentId = "${cycle.id}_${resident.uid}"
            val paymentRef = firestore.collection("maintenancePayments").document(paymentId)
            val payment = MaintenancePayment(
                id = paymentId,
                cycleId = cycle.id,
                residentUid = resident.uid,
                residentName = resident.fullName,
                flatNo = resident.flatNo,
                status = "Pending"
            )
            batch.set(paymentRef, payment)
        }
        batch.commit().await()
    }

    suspend fun updatePaymentStatus(paymentId: String, newStatus: String) {
        val paidOn = if (newStatus == "Paid") Timestamp.now() else null
        firestore.collection("maintenancePayments").document(paymentId)
            .update(mapOf("status" to newStatus, "paidOn" to paidOn))
            .await()
    }

    // NEW: Get details for a single cycle
    suspend fun getCycleDetails(cycleId: String): MaintenanceCycle? {
        return firestore.collection("maintenanceCycles").document(cycleId).get().await()
            .toObject(MaintenanceCycle::class.java)
    }

    // NEW: Update an existing cycle document
    suspend fun updateCycle(cycle: MaintenanceCycle) {
        firestore.collection("maintenanceCycles").document(cycle.id).set(cycle).await()
    }
}

