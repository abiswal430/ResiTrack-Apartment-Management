package com.example.resitrack.features.admin.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.resitrack.data.manager.AuthManager
import com.example.resitrack.data.manager.ComplaintsManager
import com.example.resitrack.data.manager.UsersManager
import com.example.resitrack.data.model.Complaint
import com.example.resitrack.data.source.AuthSource
import com.example.resitrack.data.source.ComplaintsSource
import com.example.resitrack.data.source.UsersSource
import com.example.resitrack.navigation.Screen
import com.example.resitrack.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(mainNavController: NavController) {
    val usersSource = UsersSource()
    val complaintsSource = ComplaintsSource()
    val usersManager = UsersManager(usersSource)
    val complaintsManager = ComplaintsManager(complaintsSource)
    val authSource = AuthSource()
    val authManager = AuthManager(authSource)
    val viewModel: AdminDashboardViewModel = viewModel(
        factory = AdminDashboardViewModelFactory(usersManager, complaintsManager)
    )

    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.White,
        topBar = {

            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                title = { Text("Dashboard", fontWeight = FontWeight.Bold) },
                actions = {
                    // UPDATED: Changed Settings icon to a "More" menu
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                    // Dropdown menu for additional admin actions
                    DropdownMenu(
                        containerColor = Color.White,
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Profile") },
                            onClick = {
                                mainNavController.navigate(Screen.AdminProfile.route)
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Profile"
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Maintenance Fees") },
                            onClick = {
                                mainNavController.navigate(Screen.ManageMaintenanceFees.route)
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Receipt,
                                    contentDescription = "Maintenance Fees"
                                )
                            }
                        )
                        DropdownMenuItem(

                            text = { Text("Logout") },
                            onClick = {
                                authManager.signOut()
                                mainNavController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.AdminMain.route) {
                                        inclusive = true
                                    }
                                }
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Logout"
                                )
                            }
                        )
                        // You can add other items like "Settings" here in the future
                    }
                }
            )
        },
    ) { paddingValues ->


            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                ) {
                    // Summary Cards
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            SummaryCard(
                                title = "Total Residents",
                                count = uiState.totalResidents.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            SummaryCard(
                                title = "Pending Complaints",
                                count = uiState.pendingComplaints.toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        SummaryCard(
                            title = "Resolved Complaints",
                            count = uiState.resolvedComplaints.toString(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Action Buttons
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { mainNavController.navigate(Screen.AddEditResident.addRoute()) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(0.5f),
                            ) {
                                Text("Add Resident")
                            }
                            Spacer(Modifier.width(80.dp))
                            Button(
                                onClick = { mainNavController.navigate(Screen.AddEditNotice.addRoute()) },
                                modifier = Modifier.weight(0.5f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFFE8EDF2
                                    )
                                )
                            ) {
                                Text("Post Notice", color = Color(0xFF0D141C))
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // Recent Complaints Header
                    item {
                        Text(
                            "Recent Complaints",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Recent Complaints List
                    items(uiState.recentComplaints) { complaint ->
                        ComplaintItem(complaint = complaint)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }

@Composable
fun SummaryCard(title: String, count: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.border(1.dp, Color(0xFFD1DBE8), shape = RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White // 👈 set background color here
        ),
         shape = RoundedCornerShape(8.dp) // you can enable rounded corners if needed
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                count,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun ComplaintItem(complaint: Complaint) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color = Color(0xFFE8EDF2))
                .border(
                    width = 1.dp,
                    color = Color(0xFFE8EDF2),
                    shape = RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Campaign, contentDescription = "Complaint Icon", tint = Color.Black)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("${complaint.raisedByFlatNo} - ${complaint.raisedByName}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Issue: ${complaint.subject}", color = Color.Gray, fontSize = 14.sp)
        }
        // This would ideally be a calculated "time ago" value
        Text("2d ago", color = Color.Gray, fontSize = 14.sp)
    }
}

