package com.example.resitrack.features.admin.residents

import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.resitrack.data.manager.UsersManager
import com.example.resitrack.data.model.User
import com.example.resitrack.data.source.UsersSource
import com.example.resitrack.navigation.Screen
import com.example.resitrack.ui.theme.LightGreyBackground
import com.example.resitrack.ui.theme.PrimaryBlue
import com.example.resitrack.util.Resource
import kotlinx.coroutines.launch

class ManageResidentsViewModelFactory(
    private val usersManager: UsersManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManageResidentsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ManageResidentsViewModel(usersManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// SCREEN UPDATED
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageResidentsScreen(mainNavController: NavController) {
    val usersSource = UsersSource()
    val usersManager = UsersManager(usersSource)
    val viewModel: ManageResidentsViewModel = viewModel(factory = ManageResidentsViewModelFactory(usersManager))
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.deleteResult) {
        when (val result = uiState.deleteResult) {
            is Resource.Success -> {
                coroutineScope.launch { snackbarHostState.showSnackbar(result.data!!) }
                viewModel.resetDeleteResult()
            }
            is Resource.Error -> {
                coroutineScope.launch { snackbarHostState.showSnackbar(result.message!!) }
                viewModel.resetDeleteResult()
            }
            else -> {}
        }
    }

    if (uiState.residentToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDelete() },
            title = { Text("Delete Resident") },
            text = { Text("Are you sure you want to delete '${uiState.residentToDelete?.fullName}'? This will permanently delete their account and data.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.onConfirmDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissDelete() }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),title = { Text("Residents", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { mainNavController.navigate(Screen.AddEditResident.addRoute()) }, containerColor = PrimaryBlue) {
                Icon(Icons.Default.Add, tint = Color.White, contentDescription = "Add Resident")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth().padding(16.dp).border(1.dp, Color.Black, shape = RoundedCornerShape(8.dp)),
                placeholder = { Text("Search residents...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                )
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredResidents) { resident ->
                        ResidentListItem(
                            resident = resident,
                            onEditClick = { mainNavController.navigate(Screen.AddEditResident.editRoute(resident.uid)) },
                            onDeleteClick = { viewModel.onStartDelete(resident) } // UPDATED
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResidentListItem(
    resident: User,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit // ADDED
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(resident.fullName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text("Apt ${resident.flatNo}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
        IconButton(onClick = onDeleteClick) { // ADDED
            Icon(Icons.Default.Delete, contentDescription = "Delete Resident", tint = MaterialTheme.colorScheme.error)
        }
        IconButton(onClick = onEditClick) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Resident", tint = Color.Gray)
        }
    }
}

