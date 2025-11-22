package com.example.resitrack.features.resident.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.resitrack.data.manager.AuthManager
import com.example.resitrack.data.manager.FacilitiesManager
import com.example.resitrack.data.model.Facility
import com.example.resitrack.data.model.TimeSlot
import com.example.resitrack.data.source.AuthSource
import com.example.resitrack.data.source.FacilitiesSource
import com.example.resitrack.ui.theme.PrimaryBlue
import com.example.resitrack.util.Resource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookFacilityScreen(navController: NavController) {
    val facilitiesSource = FacilitiesSource()
    val authSource = AuthSource()
    val facilitiesManager = FacilitiesManager(facilitiesSource)
    val authManager = AuthManager(authSource)
    val factory = BookFacilityViewModelFactory(facilitiesManager, authManager)
    val viewModel: BookFacilityViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.bookingResult) {
        when (val result = uiState.bookingResult) {
            is Resource.Success -> {
                coroutineScope.launch { snackbarHostState.showSnackbar(result.data!!) }
                viewModel.resetBookingResult()
                navController.popBackStack()
            }
            is Resource.Error -> {
                coroutineScope.launch { snackbarHostState.showSnackbar(result.message!!) }
                viewModel.resetBookingResult()
            }
            else -> {}
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.onDateSelected(it) }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                title = { Text("Book Facility", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                SectionTitle("Select Facility")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.facilities) { facility ->
                        FacilityChip(

                            facility = facility,
                            isSelected = uiState.selectedFacility?.id == facility.id,
                            onClick = { viewModel.onFacilitySelected(facility) }
                        )
                    }
                }

                SectionTitle("Availability")
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(uiState.selectedDate)
                }

                SectionTitle("Time Slots")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    Arrangement.Absolute.spacedBy(10.dp),
                    verticalArrangement = Arrangement.Absolute.spacedBy(8.dp)

//                    mainAxisSpacing = 8.dp,
//                    crossAxisSpacing = 8.dp
                ) {
                    uiState.availableTimeSlots.forEach { slot ->
                        val isBooked = uiState.bookedTimeSlots.contains(slot.slotId)
                        TimeSlotChip(
                            timeSlot = slot,
                            isSelected = uiState.selectedTimeSlot?.slotId == slot.slotId,
                            isBooked = isBooked,
                            onClick = { viewModel.onTimeSlotSelected(slot) }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { viewModel.bookFacility() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    
                    enabled = uiState.bookingResult !is Resource.Loading && uiState.selectedTimeSlot != null
                ) {
                    Text("Book Now")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
fun FacilityChip(facility: Facility, isSelected: Boolean, onClick: () -> Unit) {
    val colors = if (isSelected) ButtonDefaults.buttonColors(containerColor = PrimaryBlue) else ButtonDefaults.outlinedButtonColors()
    OutlinedButton(onClick = onClick, colors = colors) {
        Text(facility.name)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimeSlotChip(timeSlot: TimeSlot, isSelected: Boolean, isBooked: Boolean, onClick: () -> Unit) {
    val containerColor = when {
        isSelected -> PrimaryBlue
        isBooked -> MaterialTheme.colorScheme.inversePrimary
        else -> MaterialTheme.colorScheme.background
    }
    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isBooked -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> Color.Black
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .clickable(enabled = !isBooked, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "${timeSlot.startTime} - ${timeSlot.endTime}",
            color = contentColor
        )
    }
}
