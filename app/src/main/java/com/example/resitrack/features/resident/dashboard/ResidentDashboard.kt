package com.example.resitrack.features.resident.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.resitrack.data.manager.AuthManager
import com.example.resitrack.data.manager.NoticesManager
import com.example.resitrack.data.manager.UsersManager
import com.example.resitrack.data.source.AuthSource
import com.example.resitrack.data.source.NoticesSource
import com.example.resitrack.data.source.UsersSource
import com.example.resitrack.navigation.ResidentBottomScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentDashboardScreen(
    nestedNavController: NavController,
    onLogout: () -> Unit
) {
    val usersSource = UsersSource()
    val noticesSource = NoticesSource()
    val authSource = AuthSource()
    val usersManager = UsersManager(usersSource)
    val noticesManager = NoticesManager(noticesSource)
    val authManager = AuthManager(authSource)

    val viewModel: ResidentDashboardViewModel = viewModel(
        factory = ResidentDashboardViewModelFactory(usersManager, noticesManager, authManager)
    )
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                title = { Text("My Society", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Open navigation drawer */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                authManager.signOut()
                                onLogout() // UPDATED: Call the passed-in logout function
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout") }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .background(Color.White)
            ) {
                item {
                    Text(
                        "Quick Actions",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            QuickActionCard(
                                title = "Notices",
                                icon = Icons.Default.Campaign,
                                modifier = Modifier.weight(1f),
                                onClick = { nestedNavController.navigate(ResidentBottomScreen.ViewNotices.route) }
                            )
                            QuickActionCard(
                                title = "Complaints",
                                icon = Icons.Default.Forum,
                                modifier = Modifier.weight(1f),
                                onClick = { nestedNavController.navigate(ResidentBottomScreen.MyComplaints.route) }
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            QuickActionCard(
                                title = "Bookings",
                                icon = Icons.Default.EventAvailable,
                                modifier = Modifier.weight(1f),
                                onClick = { nestedNavController.navigate(ResidentBottomScreen.MyBookings.route) }
                            )
                            QuickActionCard(
                                title = "Profile",
                                icon = Icons.Default.Person,
                                modifier = Modifier.weight(1f),
                                onClick = { nestedNavController.navigate(ResidentBottomScreen.MyProfile.route) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                item {
                    Text(
                        "Recent Activity",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                items(uiState.recentActivity) { notice ->
                    ActivityItem(
                        icon = Icons.Default.Campaign,
                        title = notice.title,
                        subtitle = "General" // Category can be added to Notice model later
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionCard(title: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(80.dp).border(1.dp, Color(0xFFD1DBE8), shape = RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White // 👈 set background color here
        ),
        shape = RoundedCornerShape(8.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = title)
            Text(title, fontWeight = FontWeight.SemiBold)
        }
    }
}


@Composable
fun ActivityItem(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = Color.Gray, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitle, color = Color.Gray, fontSize = 14.sp)
        }
    }
}

