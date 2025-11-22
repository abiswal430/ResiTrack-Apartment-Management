package com.example.resitrack.features.auth

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.resitrack.data.manager.AuthManager
import com.example.resitrack.data.manager.NotificationsManager
import com.example.resitrack.data.source.AuthSource
import com.example.resitrack.data.source.NotificationsSource
import com.example.resitrack.navigation.Screen
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(navController: NavController) {
    val authSource = AuthSource()
    val authManager = AuthManager(authSource)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        val uid = authManager.getCurrentUserId()
        val destination = if (uid != null) {
            try {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w("FCM_TOKEN", "Fetching FCM registration token failed", task.exception)
                        return@addOnCompleteListener
                    }
                    val token = task.result
                    Log.d("FCM_TOKEN", "Initial FCM Token fetched: $token")
                    // Save the token to Firestore
                    val notificationsSource = NotificationsSource()
                    val notificationsManager = NotificationsManager(notificationsSource)
                    coroutineScope.launch {
                        notificationsManager.saveUserToken(uid, token)
                    }
                }

                // Determine role and navigate
                when (authManager.getUserRole(uid)) {
                    "admin" -> Screen.AdminMain.route
                    "resident" -> Screen.ResidentMain.route
                    else -> Screen.Login.route
                }
            } catch (e: Exception) {
                Screen.Login.route
            }
        } else {
            Screen.Login.route
        }

        navController.navigate(destination) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

