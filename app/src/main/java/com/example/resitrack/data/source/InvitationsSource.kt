package com.example.resitrack.data.source

import com.example.resitrack.data.model.Invitation
import com.example.resitrack.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class InvitationsSource {
    private val firestore = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    suspend fun createInvitation(invitation: Invitation) {
        firestore.collection("invitations").add(invitation).await()
    }

    suspend fun completeRegistration(code: String, email: String, password: String): String {
        // 1. Find the invitation document that matches the code and email
        val query = firestore.collection("invitations")
            .whereEqualTo("invitationCode", code)
            .whereEqualTo("email", email)
            .whereEqualTo("status", "Pending") // Ensure it's not already used
            .limit(1)
            .get().await()

        if (query.isEmpty) {
            throw Exception("Invalid invitation code or email, or code has already been used.")
        }
        val invitationDoc = query.documents.first()
        val invitation = invitationDoc.toObject<Invitation>()!!

        // 2. Create the user in Firebase Authentication (Client-side operation)
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val uid = authResult.user?.uid ?: throw Exception("Authentication failed.")

        // 3. Create the user document in Firestore
        val newUser = User(
            uid = uid,
            fullName = invitation.fullName,
            contactNumber = invitation.contactNumber,
            email = invitation.email,
            flatNo = invitation.flatNo,
            role = "resident"
        )
        firestore.collection("users").document(uid).set(newUser).await()

        // 4. Mark the invitation as "Completed" so it can't be used again
        firestore.collection("invitations").document(invitationDoc.id)
            .update("status", "Completed").await()

        return uid
    }
}

