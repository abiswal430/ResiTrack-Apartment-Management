package com.example.resitrack.util

import android.content.Context
import android.content.Intent
import android.net.Uri

fun sendInvitationEmail(
    context: Context,
    recipientEmail: String,
    residentName: String,
    invitationCode: String
) {
    val subject = "Your Invitation to Join ResiTrack"
    val body = """
        Hello $residentName,

        You have been invited to join your society on ResiTrack.

        Please download the app and use the following details to register:
        Email: $recipientEmail
        Invitation Code: $invitationCode

        Thank you,
        Your Society Admin
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }

    // Use a chooser to let the user pick their email app
    context.startActivity(
        Intent.createChooser(intent, "Send Invitation Email")
    )
}