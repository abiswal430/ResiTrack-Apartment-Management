package com.example.resitrack.features.resident.complaints

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.resitrack.data.manager.AuthManager
import com.example.resitrack.data.manager.ComplaintsManager
import com.example.resitrack.data.model.Complaint
import com.example.resitrack.data.source.AuthSource
import com.example.resitrack.data.source.ComplaintsSource
import com.example.resitrack.navigation.Screen
import com.example.resitrack.ui.theme.LightGreyBackground
import com.example.resitrack.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyComplaintsScreen(navController: NavController) {
    val complaintsSource = ComplaintsSource()
    val authSource = AuthSource()
    val complaintsManager = ComplaintsManager(complaintsSource)
    val authManager = AuthManager(authSource)
    val viewModel: MyComplaintsViewModel = viewModel(
        factory = MyComplaintsViewModelFactory(complaintsManager, authManager)
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                title = { Text("Complaints", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.FileNewComplaint.route) }, containerColor = PrimaryBlue) {
                Icon(Icons.Default.Add, tint = Color.White, contentDescription = "File Complaint")
            }
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
                    .background(Color.White),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "My Complaints",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                items(uiState.complaints) { complaint ->
                    ComplaintListItem(complaint = complaint)
                }
            }
        }
    }
}

@Composable
fun ComplaintListItem(complaint: Complaint) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(LightGreyBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.PriorityHigh,
                contentDescription = "Complaint",
                tint = Color.Gray,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(complaint.subject, style = MaterialTheme.typography.titleMedium)
            Text(
                "Status: ${complaint.status}",
                style = MaterialTheme.typography.bodyMedium,
                color = getStatusColor(complaint.status)
            )
        }
    }
}

private fun getStatusColor(status: String): Color {
    return when (status) {
        "Pending" -> Color(0xFFFFA500) // Orange
        "InProgress" -> Color(0xFF1E90FF) // DodgerBlue
        "Resolved" -> Color(0xFF32CD32) // LimeGreen
        else -> Color.Gray
    }
}
