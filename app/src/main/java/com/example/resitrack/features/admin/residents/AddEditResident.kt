package com.example.resitrack.features.admin.residents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.example.resitrack.data.manager.InvitationsManager
import com.example.resitrack.data.manager.UsersManager
import com.example.resitrack.data.source.InvitationsSource
import com.example.resitrack.data.source.UsersSource
import com.example.resitrack.ui.theme.LightGreyBackground
import com.example.resitrack.ui.theme.PrimaryBlue
import com.example.resitrack.util.Resource
import com.example.resitrack.util.sendInvitationEmail
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditResidentScreen(navController: NavController, backStackEntry: NavBackStackEntry) {
    val usersSource = UsersSource()
    val invitationsSource = InvitationsSource()
    val usersManager = UsersManager(usersSource)
    val invitationsManager = InvitationsManager(invitationsSource)
    val factory = AddEditResidentViewModelFactory(usersManager, invitationsManager, backStackEntry.savedStateHandle)
    val viewModel: AddEditResidentViewModel = viewModel(factory = factory)

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(uiState.saveResult) {
        when (val result = uiState.saveResult) {
            is Resource.Success -> {
                val successMessage = result.data!!
                coroutineScope.launch { snackbarHostState.showSnackbar(successMessage) }

                // Only send an email if it was an invitation
                if (!uiState.isEditMode) {
                    val code = successMessage.substringAfterLast(": ")
                    sendInvitationEmail(
                        context = context,
                        recipientEmail = uiState.email,
                        residentName = uiState.fullName,
                        invitationCode = code
                    )
                }
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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ResidentTextField(label = "Full Name", value = uiState.fullName, onValueChange = viewModel::onFullNameChange)
                ResidentTextField(label = "Contact Number", value = uiState.contactNumber, onValueChange = viewModel::onContactChange)
                ResidentTextField(
                    label = "Email Address",
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    readOnly = uiState.isEditMode // Email is read-only in edit mode
                )
                ResidentTextField(label = "Flat Number", value = uiState.flatNo, onValueChange = viewModel::onFlatNoChange)

                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { viewModel.onSaveClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    enabled = uiState.saveResult !is Resource.Loading
                ) {
                    Text(text = uiState.buttonText)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    readOnly: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = { Text(label) },
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = LightGreyBackground,
            unfocusedContainerColor = LightGreyBackground,
            disabledContainerColor = LightGreyBackground,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
        ),
        readOnly = readOnly,
        singleLine = true
    )
}

