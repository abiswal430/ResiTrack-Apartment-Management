package com.example.resitrack.features.admin.facilities

import android.annotation.SuppressLint
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.example.resitrack.data.manager.FacilitiesManager
import com.example.resitrack.data.model.TimeSlot
import com.example.resitrack.data.source.FacilitiesSource
import com.example.resitrack.ui.theme.LightGreyBackground
import com.example.resitrack.ui.theme.PrimaryBlue
import com.example.resitrack.util.Resource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFacilityScreen(navController: NavController, backStackEntry: NavBackStackEntry) {
    val facilitiesSource = FacilitiesSource()
    val facilitiesManager = FacilitiesManager(facilitiesSource)
    val factory =
        AddEditFacilityViewModelFactory(facilitiesManager, backStackEntry.savedStateHandle)
    val viewModel: AddEditFacilityViewModel = viewModel(factory = factory)

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showAddTimeSlotDialog by remember { mutableStateOf(false) }

    if (showAddTimeSlotDialog) {
        AddTimeSlotDialog(
            onDismiss = { showAddTimeSlotDialog = false },
            onAdd = { startTime, endTime ->
                viewModel.onAddTimeSlot(startTime, endTime)
                showAddTimeSlotDialog = false
            }
        )
    }

    LaunchedEffect(uiState.saveResult) {
        when (val result = uiState.saveResult) {
            is Resource.Success -> {
                coroutineScope.launch { snackbarHostState.showSnackbar(result.data!!) }
                viewModel.resetSaveResult()
                navController.popBackStack()
            }

            is Resource.Error -> {
                coroutineScope.launch { snackbarHostState.showSnackbar(result.message!!) }
                viewModel.resetSaveResult()
            }

            else -> {}
        }
    }

    Scaffold(
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                title = { Text(uiState.screenTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Facility Name") },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = LightGreyBackground,
                        unfocusedContainerColor = LightGreyBackground
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    placeholder = { Text("Description") },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = LightGreyBackground,
                        unfocusedContainerColor = LightGreyBackground
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
                SettingSwitch(
                    title = "Booking Required",
                    checked = uiState.bookingRequired,
                    onCheckedChange = viewModel::onBookingRequiredChange
                )
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                SettingSwitch(
                    title = "Facility is Available",
                    checked = uiState.isAvailable,
                    onCheckedChange = viewModel::onAvailabilityChange
                )

                if (uiState.bookingRequired) {
                    Spacer(modifier = Modifier.height(24.dp))
                    TimeSlotManagementSection(
                        timeSlots = uiState.timeSlots,
                        onAddTimeSlotClicked = { showAddTimeSlotDialog = true },
                        onRemoveTimeSlot = viewModel::onRemoveTimeSlot
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        viewModel.saveFacility()

                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    enabled = uiState.saveResult !is Resource.Loading
                ) {
                    if (uiState.saveResult is Resource.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(uiState.buttonText)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SettingSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun TimeSlotManagementSection(
    timeSlots: List<TimeSlot>,
    onAddTimeSlotClicked: () -> Unit,
    onRemoveTimeSlot: (TimeSlot) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Available Time Slots",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            TextButton(onClick = onAddTimeSlotClicked) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Time Slot",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (timeSlots.isEmpty()) {
            Text("No time slots added.", color = Color.Gray)
        } else {
            timeSlots.forEach { slot ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${slot.startTime} - ${slot.endTime}", modifier = Modifier.weight(1f))
                    IconButton(onClick = { onRemoveTimeSlot(slot) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove Time Slot",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTimeSlotDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var startTime by remember { mutableStateOf("09:00 AM") }
    var endTime by remember { mutableStateOf("10:00 AM") }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    // UPDATED: is24Hour is now false
    val startTimePickerState =
        rememberTimePickerState(initialHour = 9, initialMinute = 0, is24Hour = false)
    val endTimePickerState =
        rememberTimePickerState(initialHour = 10, initialMinute = 0, is24Hour = false)

    if (showStartTimePicker) {
        TimePickerDialog(
            state = startTimePickerState,
            onDismiss = { showStartTimePicker = false },
            onConfirm = {
                // UPDATED: Logic to format time in 12-hour format with AM/PM
                val hour = startTimePickerState.hour
                val minute = startTimePickerState.minute
                val amPm = if (hour < 12) "AM" else "PM"
                val formattedHour = when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
                startTime = String.format("%02d:%02d %s", formattedHour, minute, amPm)
                showStartTimePicker = false
            }
        )
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            state = endTimePickerState,
            onDismiss = { showEndTimePicker = false },
            onConfirm = {
                // UPDATED: Logic to format time in 12-hour format with AM/PM
                val hour = endTimePickerState.hour
                val minute = endTimePickerState.minute
                val amPm = if (hour < 12) "AM" else "PM"
                val formattedHour = when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
                endTime = String.format("%02d:%02d %s", formattedHour, minute, amPm)
                showEndTimePicker = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Time Slot") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Select start and end time for the new slot.")
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Start Time", style = MaterialTheme.typography.labelMedium)
                        OutlinedButton(onClick = { showStartTimePicker = true }) {
                            Text(startTime)
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("End Time", style = MaterialTheme.typography.labelMedium)
                        OutlinedButton(onClick = { showEndTimePicker = true }) {
                            Text(endTime)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(startTime, endTime) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    state: TimePickerState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = {
            Column(
                modifier = Modifier.padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = state)
            }
        }
    )
}

