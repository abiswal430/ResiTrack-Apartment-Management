package com.example.resitrack.features.admin


import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.resitrack.features.admin.complaints.ManageComplaintsScreen
import com.example.resitrack.features.admin.components.AdminBottomNavBar
import com.example.resitrack.features.admin.dashboard.AdminDashboardScreen
import com.example.resitrack.features.admin.facilities.ManageFacilitiesScreen
import com.example.resitrack.features.admin.notices.ManageNoticesScreen
import com.example.resitrack.features.admin.residents.ManageResidentsScreen
import com.example.resitrack.navigation.AdminBottomScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(mainNavController: NavController) {
    val nestedNavController = rememberNavController()

    Scaffold(
        containerColor = Color.White,
        bottomBar = { AdminBottomNavBar(navController = nestedNavController) }) { paddingValues ->
        NavHost(
            navController = nestedNavController,
            startDestination = AdminBottomScreen.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(AdminBottomScreen.Dashboard.route) {
                AdminDashboardScreen(mainNavController)
            }
            composable(AdminBottomScreen.ManageResidents.route) {
                ManageResidentsScreen(mainNavController)
            }
            composable(AdminBottomScreen.ManageNotices.route) {
                ManageNoticesScreen(mainNavController)
            }
            composable(AdminBottomScreen.ViewAllComplaints.route) {
                ManageComplaintsScreen(mainNavController)
            }
            composable(AdminBottomScreen.ManageFacilities.route) {
                ManageFacilitiesScreen(mainNavController = mainNavController)
            }
        }
    }
}

