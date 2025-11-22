package com.example.resitrack.features.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.resitrack.data.manager.InvitationsManager
import com.example.resitrack.data.source.InvitationsSource
import com.example.resitrack.features.admin.residents.ResidentTextField
import com.example.resitrack.util.Resource
import kotlinx.coroutines.launch

// Screen UI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(navController: NavController) {
    val source = InvitationsSource()
    val manager = InvitationsManager(source)
    val viewModel: RegistrationViewModel = viewModel(factory = RegistrationViewModelFactory(manager))
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Register Account", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ResidentTextField(label = "Invitation Code", value = uiState.invitationCode, onValueChange = viewModel::onCodeChange)
            ResidentTextField(label = "Email Address", value = uiState.email, onValueChange = viewModel::onEmailChange)
            ResidentTextField(label = "Password", value = uiState.password, onValueChange = viewModel::onPasswordChange)
            ResidentTextField(label = "Confirm Password", value = uiState.confirmPassword, onValueChange = viewModel::onConfirmPasswordChange)

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { viewModel.registerUser() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = uiState.saveResult !is Resource.Loading
            ) {
                Text("Complete Registration")
            }
        }
    }
}

