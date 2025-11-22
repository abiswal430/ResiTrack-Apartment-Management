package com.example.resitrack.features.resident.components

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.resitrack.navigation.ResidentBottomScreen
import com.example.resitrack.ui.theme.LightGreyBackground
import com.example.resitrack.ui.theme.White

data class ResidentBottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun ResidentBottomNavBar(navController: NavController) {
    val items = listOf(
        ResidentBottomNavItem("Dashboard", Icons.Default.Home, ResidentBottomScreen.Dashboard.route),
        ResidentBottomNavItem("Notices", Icons.Default.Campaign, ResidentBottomScreen.ViewNotices.route),
        ResidentBottomNavItem("Complaints", Icons.Default.Forum, ResidentBottomScreen.MyComplaints.route),
        ResidentBottomNavItem("Bookings", Icons.Default.EventAvailable, ResidentBottomScreen.MyBookings.route),
        ResidentBottomNavItem("Profile", Icons.Default.Person, ResidentBottomScreen.MyProfile.route)
    )

    NavigationBar(modifier = Modifier.height(104.dp), containerColor = White, contentColor = LightGreyBackground) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
//                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) { saveState = true }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = LightGreyBackground,

                )
            )
        }
    }
}
