package com.example.resitrack.features.admin.notices

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.example.resitrack.data.manager.NoticesManager
import com.example.resitrack.data.source.NoticesSource
import com.example.resitrack.ui.theme.LightGreyBackground
import com.example.resitrack.ui.theme.PrimaryBlue
import com.example.resitrack.util.Resource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoticeScreen(navController: NavController, backStackEntry: NavBackStackEntry) {
    val noticesSource = NoticesSource()
    val noticesManager = NoticesManager(noticesSource)
    val factory = AddEditNoticeViewModelFactory(noticesManager, backStackEntry.savedStateHandle)
    val viewModel: AddEditNoticeViewModel = viewModel(factory = factory)

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

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
                    value = uiState.title,
                    onValueChange = viewModel::onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Title") },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = LightGreyBackground,
                        unfocusedContainerColor = LightGreyBackground,
                        disabledContainerColor = LightGreyBackground,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.message,
                    onValueChange = viewModel::onMessageChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    placeholder = { Text("Message") },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = LightGreyBackground,
                        unfocusedContainerColor = LightGreyBackground,
                        disabledContainerColor = LightGreyBackground,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // NEW: Switch to control the active status of the notice
                SettingSwitch(
                    title = "Notice is Active",
                    checked = uiState.isActive,
                    onCheckedChange = viewModel::onIsActiveChange
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { viewModel.saveNotice() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    enabled = uiState.saveResult !is Resource.Loading
                ) {
                    if (uiState.saveResult is Resource.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
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

