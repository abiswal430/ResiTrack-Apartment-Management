package com.example.resitrack.data.manager

import com.example.resitrack.data.model.Invitation
import com.example.resitrack.data.source.InvitationsSource

class InvitationsManager(private val source: InvitationsSource) {
    suspend fun createInvitation(invitation: Invitation) = source.createInvitation(invitation)

    suspend fun completeRegistration(code: String, email: String, password: String) =
        source.completeRegistration(code, email, password)
}

