package com.example.resitrack.data.source

import com.example.resitrack.data.model.Facility
import com.example.resitrack.data.model.FacilityBooking
import com.example.resitrack.data.model.UserBooking
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FacilitiesSource {
    private val firestore = FirebaseFirestore.getInstance()

    fun getAllFacilitiesFlow(): Flow<List<Facility>> = callbackFlow {
        val listener = firestore.collection("facilities")
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

    suspend fun getFacilityDetails(facilityId: String): Facility? {
        return firestore.collection("facilities").document(facilityId).get().await().toObject<Facility>()
    }

    fun getBookingDetailsFlow(facilityId: String, date: Date): Flow<FacilityBooking?> = callbackFlow {
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
        val documentId = "${facilityId}_$dateString"

        val listener = firestore.collection("facilityBookings").document(documentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.toObject<FacilityBooking>())
                } else {
                    trySend(null) // Send null if no booking document exists for that day
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun addFacility(facility: Facility) {
        // Use the custom ID for the document
        firestore.collection("facilities").document(facility.id).set(facility).await()
    }

    suspend fun updateFacility(facility: Facility) {
        firestore.collection("facilities").document(facility.id).set(facility).await()
    }

    suspend fun deleteFacility(facilityId: String) {
        firestore.collection("facilities").document(facilityId).delete().await()
    }

    fun getFacilityBookingsFlow(facilityId: String, date: String): Flow<FacilityBooking?> = callbackFlow {
        val docId = "${facilityId}_$date"
        val listener = firestore.collection("facilityBookings").document(docId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.toObject<FacilityBooking>())
                } else {
                    trySend(null) // Send null if no booking document exists for that day
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun bookSlot(
        userId: String,
        facility: Facility,
        date: String,
        timeSlot: com.example.resitrack.data.model.TimeSlot
    ) {
        val bookingDocId = "${facility.id}_$date"
        val facilityBookingRef = firestore.collection("facilityBookings").document(bookingDocId)
        val userBookingRef = firestore.collection("users").document(userId)
            .collection("bookings").document()

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(facilityBookingRef)
            val bookedSlots = snapshot.toObject<FacilityBooking>()?.bookedSlots ?: emptyMap()

            if (bookedSlots.containsKey(timeSlot.slotId)) {
                throw Exception("Slot is already booked.")
            }

            val newBookingData = mapOf("bookedSlots.${timeSlot.slotId}" to userId)
            if (snapshot.exists()) {
                transaction.update(facilityBookingRef, newBookingData)
            } else {
                val newBooking = FacilityBooking(id = bookingDocId, facilityId = facility.id, date = date, bookedSlots = mapOf(timeSlot.slotId to userId))
                transaction.set(facilityBookingRef, newBooking)
            }

            val userBooking = UserBooking(
                id = userBookingRef.id,
                facilityId = facility.id,
                facilityName = facility.name,
                date = date,
                slotId = timeSlot.slotId,
                startTime = timeSlot.startTime,
                endTime = timeSlot.endTime,
                bookedAt = Timestamp.now()
            )
            transaction.set(userBookingRef, userBooking)
        }.await()
    }

    fun getUserBookingsFlow(userId: String): Flow<List<UserBooking>> = callbackFlow {
        val listener = firestore.collection("users").document(userId).collection("bookings")
            .orderBy("bookedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                snapshot?.let { trySend(it.toObjects()) }
            }
        awaitClose { listener.remove() }
    }
}
