package com.example.resitrack.features.profile

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
import androidx.navigation.NavController
import com.example.resitrack.data.manager.AuthManager
import com.example.resitrack.data.manager.UsersManager
import com.example.resitrack.data.source.AuthSource
import com.example.resitrack.data.source.UsersSource
import com.example.resitrack.features.admin.residents.ResidentTextField
import com.example.resitrack.ui.theme.PrimaryBlue
import com.example.resitrack.util.Resource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {
    val usersSource = UsersSource()
    val authSource = AuthSource()
    val usersManager = UsersManager(usersSource)
    val authManager = AuthManager(authSource)
    val factory = EditProfileViewModelFactory(usersManager, authManager)
    val viewModel: EditProfileViewModel = viewModel(factory = factory)

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
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ResidentTextField(
                    label = "Full Name",
                    value = uiState.fullName,
                    onValueChange = viewModel::onFullNameChange
                )
                ResidentTextField(
                    label = "Contact Number",
                    value = uiState.contactNumber,
                    onValueChange = viewModel::onContactNumberChange
                )

                ResidentTextField(
                    label = "Flat Number",
                    value = uiState.flatNo,
                    onValueChange = viewModel::onFlatNoChange
                )

                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { viewModel.saveProfile() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    enabled = uiState.saveResult !is Resource.Loading
                ) {
                    Text("Save Changes")
                }
            }
        }
    }
}

