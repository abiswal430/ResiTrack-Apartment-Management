package com.example.resitrack.features.admin.maintenance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.resitrack.data.manager.MaintenanceManager
import com.example.resitrack.data.model.MaintenanceCycle
import com.example.resitrack.data.model.MaintenancePayment
import com.example.resitrack.data.source.MaintenanceSource
import com.example.resitrack.data.source.UsersSource
import com.example.resitrack.navigation.Screen
import com.example.resitrack.util.Resource
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageMaintenanceScreen(navController: NavController) {
    val maintenanceSource = MaintenanceSource()
    val usersSource = UsersSource()
    val manager = MaintenanceManager(maintenanceSource, usersSource)
    // Import the ViewModel from its own file
    val viewModel: ManageMaintenanceViewModel =
        viewModel(factory = ManageMaintenanceViewModelFactory(manager))
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.createCycleResult) {
        when (val result = uiState.createCycleResult) {
            is Resource.Success -> coroutineScope.launch { snackbarHostState.showSnackbar(result.data!!) }
            is Resource.Error -> coroutineScope.launch { snackbarHostState.showSnackbar(result.message!!) }
            else -> {}
        }
        viewModel.resetCreateResult()
    }

    if (showCreateDialog) {
        CreateCycleDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, amount, date ->
                viewModel.createNewCycle(title, amount, date)
                showCreateDialog = false
            }
        )
    }

    Scaffold(
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                title = { Text("Maintenance Fees", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AddEditMaintenanceCycle.addRoute()) }) {
                Icon(Icons.Default.Add, contentDescription = "Create Cycle")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.cycles) { cycle ->
                    MaintenanceCycleItem(
                        cycle = cycle,
                        isExpanded = uiState.expandedCycleId == cycle.id,
                        payments = uiState.paymentsForSelectedCycle,
                        onCycleClicked = { viewModel.onCycleSelected(cycle.id) },
                        onPaymentStatusChanged = viewModel::updatePaymentStatus,
                        onEditClicked = {
                            navController.navigate(
                                Screen.AddEditMaintenanceCycle.editRoute(
                                    cycle.id
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MaintenanceCycleItem(
    cycle: MaintenanceCycle,
    isExpanded: Boolean,
    payments: List<MaintenancePayment>,
    onCycleClicked: () -> Unit,
    onPaymentStatusChanged: (String, String) -> Unit,
    onEditClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCycleClicked)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                cycle.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Amount: ₹${cycle.amountDue} | Due: ${
                    cycle.dueDate?.toDate()?.let { formatDate(it) }
                }",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        IconButton(onClick = onEditClicked) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Cycle")
        }
    }
    AnimatedVisibility(visible = isExpanded) {
        Column {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )
            payments.forEach { payment ->
                PaymentStatusRow(payment = payment, onStatusChanged = onPaymentStatusChanged)
            }
        }
    }


}

@Composable
fun PaymentStatusRow(payment: MaintenancePayment, onStatusChanged: (String, String) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(payment.residentName, fontWeight = FontWeight.SemiBold)
            Text(payment.flatNo, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Switch(
            checked = payment.status == "Paid",
            onCheckedChange = { onStatusChanged(payment.id, payment.status) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCycleDialog(onDismiss: () -> Unit, onCreate: (String, Double, Date) -> Unit) {
    var title by remember { mutableStateOf("") }
    // CORRECTED: Typo fixed from mutableStateof to mutableStateOf
    var amount by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf<Date?>(null) }
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        // The returned millis is in UTC, adjust for local timezone
                        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        calendar.timeInMillis = it
                        dueDate = calendar.time
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Maintenance Cycle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Cycle Title (e.g., Oct 2025)") })
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount Due") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = dueDate?.let { formatDate(it) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Due Date") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amountDouble = amount.toDoubleOrNull()
                if (title.isNotBlank() && amountDouble != null && dueDate != null) {
                    onCreate(title, amountDouble, dueDate!!)
                }
            }) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
    return formatter.format(date)
}

