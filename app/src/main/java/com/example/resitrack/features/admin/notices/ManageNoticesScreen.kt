package com.example.resitrack.features.admin.notices

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.resitrack.data.manager.NoticesManager
import com.example.resitrack.data.model.Notice
import com.example.resitrack.data.source.NoticesSource
import com.example.resitrack.navigation.Screen
import com.example.resitrack.ui.theme.LightGreyBackground
import com.example.resitrack.ui.theme.PrimaryBlue
import com.example.resitrack.util.Resource
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageNoticesScreen(mainNavController: NavController) {
    val noticesSource = NoticesSource()
    val noticesManager = NoticesManager(noticesSource)
    val viewModel: ManageNoticesViewModel = viewModel(factory = ManageNoticesViewModelFactory(noticesManager))
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

    if (uiState.noticeToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDelete() },
            title = { Text("Delete Notice") },
            text = { Text("Are you sure you want to delete the notice titled '${uiState.noticeToDelete?.title}'?") },
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
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),title = { Text("Manage Notices", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { mainNavController.navigate(Screen.AddEditNotice.addRoute()) }, containerColor = PrimaryBlue) {
                Icon(Icons.Default.Add, contentDescription = "Post Notice", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth().padding(16.dp).border(1.dp, Color.Black, shape = RoundedCornerShape(8.dp)),
                placeholder = { Text("Search notices...") },
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
                    items(uiState.filteredNotices) { notice ->
                        NoticeListItem(
                            notice = notice,
                            onEditClick = { mainNavController.navigate(Screen.AddEditNotice.editRoute(notice.id)) },
                            onDeleteClick = { viewModel.onStartDelete(notice) } // UPDATED
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoticeListItem(
    notice: Notice,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit // ADDED
) {
    Card(modifier = Modifier.border(1.dp, Color(0xFFD1DBE8), shape = RoundedCornerShape(8.dp)).fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White // 👈 set background color here
        ),
        shape = RoundedCornerShape(8.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(notice.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notice.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Posted: ${notice.createdAt?.toDate()?.let { formatDate(it) } ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            IconButton(onClick = onDeleteClick) { // ADDED
                Icon(Icons.Default.Delete, contentDescription = "Delete Notice", tint = MaterialTheme.colorScheme.error)
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Notice", tint = Color.Gray)
            }
        }
    }
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
    return formatter.format(date)
}

