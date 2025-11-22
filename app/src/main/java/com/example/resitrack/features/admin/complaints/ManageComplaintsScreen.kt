package com.example.resitrack.features.admin.complaints

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.resitrack.data.manager.ComplaintsManager
import com.example.resitrack.data.model.Complaint
import com.example.resitrack.data.source.ComplaintsSource
import com.example.resitrack.navigation.Screen
import com.example.resitrack.ui.theme.LightGreyBackground
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageComplaintsScreen(mainNavController: NavController) {
    val complaintsSource = ComplaintsSource()
    val complaintsManager = ComplaintsManager(complaintsSource)
    val viewModel: ManageComplaintsViewModel = viewModel(factory = ManageComplaintsViewModelFactory(complaintsManager))
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),title = { Text("Complaints", fontWeight = FontWeight.Bold) })
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp).border(1.dp, Color.Black, shape = RoundedCornerShape(8.dp)),
                placeholder = { Text("Search complaints...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusFilterDropDown(
                    selectedStatus = uiState.statusFilter,
                    onStatusSelected = viewModel::onStatusFilterChange
                )
                // Date filter can be added here
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.filteredComplaints) { complaint ->
                        ComplaintListItem(
                            complaint = complaint,
                            onClick = {
                                mainNavController.navigate(Screen.AdminComplaintDetails.createRoute(complaint.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ComplaintListItem(complaint: Complaint, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable(onClick = onClick)) {
        Text(complaint.raisedByName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.DarkGray)) {
                    append(complaint.subject)
                }
                append(" \u00B7 ") // Dot separator
                withStyle(style = SpanStyle(color = getStatusColor(complaint.status))) {
                    append(complaint.status)
                }
            },
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = complaint.createdAt?.toDate()?.let { formatDate(it, "MMM dd, yyyy") } ?: "N/A",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun StatusFilterDropDown(selectedStatus: String, onStatusSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val statuses = listOf("All", "Pending", "InProgress", "Resolved")

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selectedStatus)
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            statuses.forEach { status ->
                DropdownMenuItem(text = { Text(status) }, onClick = {
                    onStatusSelected(status)
                    expanded = false
                })
            }
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

fun formatDate(date: Date, pattern: String): String {
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(date)
}
