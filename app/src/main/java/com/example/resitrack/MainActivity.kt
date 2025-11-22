package com.example.resitrack

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.resitrack.navigation.AppNavigation
import com.example.resitrack.ui.theme.ResiTrackTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ResiTrackTheme {
                Surface(
                   color = MaterialTheme.colorScheme.background,

                ){
                    NotificationPermissionHandler()
                    AppNavigation()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationPermissionHandler() {
    // Notification permission is only required on Android 13 (TIRAMISU) and above.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

        val notificationPermissionState = rememberPermissionState(
            permission = Manifest.permission.POST_NOTIFICATIONS
        )

        if (!notificationPermissionState.status.isGranted) {
            LaunchedEffect(key1 = true) {
                notificationPermissionState.launchPermissionRequest()
            }
        }
    }
}