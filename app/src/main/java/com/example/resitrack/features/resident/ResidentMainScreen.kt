package com.example.resitrack.features.resident

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.resitrack.features.profile.ProfileScreen
import com.example.resitrack.features.resident.bookings.MyBookingsScreen
import com.example.resitrack.features.resident.complaints.MyComplaintsScreen
import com.example.resitrack.features.resident.components.ResidentBottomNavBar
import com.example.resitrack.features.resident.dashboard.ResidentDashboardScreen
import com.example.resitrack.features.resident.notices.ViewNoticesScreen
import com.example.resitrack.navigation.ResidentBottomScreen

@Composable
fun ResidentMainScreen(mainNavController: NavController, onLogout: () -> Unit) {
    val nestedNavController = rememberNavController()
    Scaffold(
        containerColor = Color.White,
        bottomBar = { ResidentBottomNavBar(navController = nestedNavController) }
    ) { paddingValues ->
        NavHost(
            navController = nestedNavController,
            startDestination = ResidentBottomScreen.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(ResidentBottomScreen.Dashboard.route) { ResidentDashboardScreen(nestedNavController, onLogout) }
            composable(ResidentBottomScreen.ViewNotices.route) { ViewNoticesScreen(mainNavController) }
            composable(ResidentBottomScreen.MyComplaints.route) { MyComplaintsScreen(mainNavController) }
            composable(ResidentBottomScreen.MyBookings.route) { MyBookingsScreen(mainNavController) }
            composable(ResidentBottomScreen.MyProfile.route) { ProfileScreen(mainNavController) }
        }
    }
}
