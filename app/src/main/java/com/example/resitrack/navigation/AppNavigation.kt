package com.example.resitrack.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.resitrack.data.manager.AuthManager
import com.example.resitrack.data.source.AuthSource
import com.example.resitrack.features.admin.AdminMainScreen
import com.example.resitrack.features.admin.complaints.ComplaintDetailsScreen
import com.example.resitrack.features.admin.facilities.AddEditFacilityScreen
import com.example.resitrack.features.admin.maintenance.AddEditMaintenanceCycleScreen
import com.example.resitrack.features.admin.maintenance.ManageMaintenanceScreen
import com.example.resitrack.features.admin.notices.AddEditNoticeScreen
import com.example.resitrack.features.admin.residents.AddEditResidentScreen
import com.example.resitrack.features.auth.LoginScreen
import com.example.resitrack.features.auth.RegistrationScreen
import com.example.resitrack.features.auth.SplashScreen
import com.example.resitrack.features.profile.EditProfileScreen
import com.example.resitrack.features.profile.ProfileScreen
import com.example.resitrack.features.resident.ResidentMainScreen
import com.example.resitrack.features.resident.bookings.BookFacilityScreen
import com.example.resitrack.features.resident.complaints.AddComplaintScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authSource = AuthSource()
    val authManager = AuthManager(authSource)
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        //Splash Screen
        composable(route = Screen.Splash.route) { SplashScreen(navController) }

        // --- Auth Flow ---
        composable(route = Screen.Login.route) { LoginScreen(navController) }
        composable(route = Screen.Registration.route) { RegistrationScreen(navController) }

        // --- Main App Containers ---
        composable(route = Screen.AdminMain.route) { AdminMainScreen(mainNavController = navController) }
        composable(route = Screen.ResidentMain.route) {
            ResidentMainScreen(
                mainNavController = navController,
                onLogout = {
                    authManager.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ResidentMain.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        // --- Admin Screens without Bottom Nav ---
        composable(
            route = Screen.AdminComplaintDetails.route,
            arguments = listOf(navArgument("complaintId") { type = NavType.StringType })
        ) { backStackEntry ->
            ComplaintDetailsScreen(navController, backStackEntry)
        }

        composable(
            route = Screen.AddEditResident.routeWithArgs,
            arguments = listOf(navArgument(Screen.AddEditResident.residentIdArg) {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            AddEditResidentScreen(navController, backStackEntry)
        }

        composable(route = Screen.AddEditNotice.routeWithArgs, arguments = listOf(navArgument(Screen.AddEditNotice.noticeIdArg) {
            type = NavType.StringType
            nullable = true
        })) { backStackEntry ->
            AddEditNoticeScreen(navController, backStackEntry)
        }
        composable(route = Screen.AddEditFacility.routeWithArgs, arguments = listOf(navArgument(Screen.AddEditFacility.facilityIdArg){
            type = NavType.StringType
            nullable = true
        })) { backStackEntry ->
            AddEditFacilityScreen(navController, backStackEntry)
        }

        composable(route = Screen.ManageMaintenanceFees.route) { ManageMaintenanceScreen(navController) }

        composable(
            route = Screen.AddEditMaintenanceCycle.routeWithArgs,
            arguments = listOf(navArgument(Screen.AddEditMaintenanceCycle.cycleIdArg) {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            AddEditMaintenanceCycleScreen(navController, backStackEntry)
        }

        // --- Resident Screens without Bottom Nav ---
        composable(route = Screen.FileNewComplaint.route) { AddComplaintScreen(navController) }
        composable(route = Screen.BookFacility.route) { BookFacilityScreen(navController) }
        composable(route = Screen.EditMyProfile.route) { EditProfileScreen(navController) }
        composable(route = Screen.AdminProfile.route) { ProfileScreen(navController) }



    }
}