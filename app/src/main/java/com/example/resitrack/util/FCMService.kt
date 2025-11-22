package com.example.resitrack.util

import android.util.Log
import com.example.resitrack.data.manager.NotificationsManager
import com.example.resitrack.data.source.NotificationsSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ResiTrackFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
//        Log.d("FCM_TOKEN", "New token generated: $token")

        FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
            sendTokenToServer(userId, token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Message received from: ${remoteMessage.from}")

        // Check if the message contains a notification payload and show it.
        remoteMessage.notification?.let { notification ->
            Log.d("FCM", "Notification Title: ${notification.title}")
            Log.d("FCM", "Notification Body: ${notification.body}")

            // Use the utility to display the notification
            NotificationUtils.showNotification(
                context = this,
                title = notification.title ?: "New Notification",
                message = notification.body ?: ""
            )
        }
    }
    @OptIn(DelicateCoroutinesApi::class)
    private fun sendTokenToServer(userId: String, token: String) {
        GlobalScope.launch {
            try {
                val source = NotificationsSource()
                val manager = NotificationsManager(source)
                manager.saveUserToken(userId, token)
//                Log.d("FCM_TOKEN", "Token successfully saved to Firestore for user: $userId")
            } catch (e: Exception) {
                Log.e("FCM_TOKEN", "Error saving token to Firestore", e)
            }
        }
    }
}

