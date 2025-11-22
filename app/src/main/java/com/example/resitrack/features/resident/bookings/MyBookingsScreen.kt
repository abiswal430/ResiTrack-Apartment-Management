package com.example.resitrack.features.resident.bookings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.resitrack.data.manager.AuthManager
import com.example.resitrack.data.manager.FacilitiesManager
import com.example.resitrack.data.model.UserBooking
import com.example.resitrack.data.source.AuthSource
import com.example.resitrack.data.source.FacilitiesSource
import com.example.resitrack.navigation.Screen
import com.example.resitrack.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(navController: NavController) {
    val facilitiesSource = FacilitiesSource()
    val authSource = AuthSource()
    val facilitiesManager = FacilitiesManager(facilitiesSource)
    val authManager = AuthManager(authSource)
    val viewModel: MyBookingsViewModel = viewModel(factory = MyBookingsViewModelFactory(facilitiesManager, authManager))
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                title = { Text("My Bookings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.BookFacility.route) }, containerColor = PrimaryBlue) {
                Icon(Icons.Default.Add, tint = Color.White, contentDescription = "Book a Facility")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.bookings) { booking ->
                    BookingListItem(booking = booking)
                }
            }
        }
    }
}

@Composable
fun BookingListItem(booking: UserBooking) {
    Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFD1DBE8), shape = RoundedCornerShape(8.dp)),
    colors = CardDefaults.cardColors(
        containerColor = Color.White // 👈 set background color here
    ),
    shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(booking.facilityName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(4.dp))
            Text("Date: ${booking.date}", style = MaterialTheme.typography.bodyMedium)
            Text("Time: ${booking.startTime} - ${booking.endTime}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}
