package com.example.resitrack.features.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.resitrack.navigation.AdminBottomScreen
import com.example.resitrack.ui.theme.LightBlue
import com.example.resitrack.ui.theme.LightGreyBackground
import com.example.resitrack.ui.theme.PrimaryBlue
import com.example.resitrack.ui.theme.White

// Data class to represent each item in the bottom nav
data class AdminBottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun AdminBottomNavBar(navController: NavController) {
    val items = listOf(
        AdminBottomNavItem("Dashboard", Icons.Default.Dashboard, AdminBottomScreen.Dashboard.route),
        AdminBottomNavItem(
            "Residents",
            Icons.Default.Groups,
            AdminBottomScreen.ManageResidents.route
        ),
        AdminBottomNavItem(
            "Notices",
            Icons.Default.Campaign,
            AdminBottomScreen.ManageNotices.route
        ),
        AdminBottomNavItem(
            "Complaints",
            Icons.Default.ReportProblem,
            AdminBottomScreen.ViewAllComplaints.route
        ),
        AdminBottomNavItem(
            "Facilities",
            Icons.Default.Apartment,
            AdminBottomScreen.ManageFacilities.route
        )
    )

    NavigationBar(
        containerColor = White, contentColor = LightBlue,
        modifier = Modifier.height(94.dp)
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                modifier = Modifier.background(color = Color.White),
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryBlue,
                    indicatorColor = LightGreyBackground
                ),
                icon = { Icon(item.icon, contentDescription = item.label) },
//                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination to avoid building a large back stack
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        // Avoid multiple copies of the same destination when re-selecting the same item
                        launchSingleTop = true
                        // Restore state when re-selecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}
