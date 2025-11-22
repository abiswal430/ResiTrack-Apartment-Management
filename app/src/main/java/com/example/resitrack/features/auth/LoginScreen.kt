package com.example.resitrack.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.resitrack.data.manager.AuthManager
import com.example.resitrack.data.manager.NotificationsManager
import com.example.resitrack.data.source.AuthSource
import com.example.resitrack.data.source.NotificationsSource
import com.example.resitrack.navigation.Screen
import com.example.resitrack.ui.theme.LightGreyBackground
import com.example.resitrack.ui.theme.PrimaryBlue
import com.example.resitrack.ui.theme.ResiTrackTheme
import com.example.resitrack.util.Resource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val authSource = remember { AuthSource() }
    val authManager = remember { AuthManager(authSource) }

    val notificationsSource = NotificationsSource()
    val notificationsManager = NotificationsManager(notificationsSource)

    val viewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory(authManager, notificationsManager))
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var passwordVisibility by remember { mutableStateOf(false) }

    // Observe login result
    LaunchedEffect(viewModel.loginResult.value) {
        when (val result = viewModel.loginResult.value) {
            is Resource.Loading -> {
                // Show loading indicator
            }
            is Resource.Success -> {
                if (result.data != null && result.data.isNotEmpty()) {
                    // Login successful, navigate based on role
                    val userRole = authManager.getUserRole(result.data)
                    viewModel.resetLoginResult() // Reset to prevent re-triggering
                    when (userRole) {
                        "admin" -> navController.navigate(Screen.AdminMain.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                        "resident" -> navController.navigate(Screen.ResidentMain.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                        else -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Unknown user role. Please contact support.")
                            }
                        }
                    }
                }
            }
            is Resource.Error -> {
                if (result.message != null && result.message.isNotEmpty()) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(result.message)
                    }
                    viewModel.resetLoginResult() // Reset to prevent re-triggering
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome, Login",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Email Field
            Text(
                text = "Email",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 4.dp),
                textAlign = TextAlign.Start
            )
            OutlinedTextField(
                value = viewModel.email.value,
                onValueChange = viewModel::onEmailChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 14.dp),
                placeholder = { Text("Enter your email", fontSize =  14.sp, modifier = Modifier.padding(vertical = 0.dp)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = LightGreyBackground,
                    unfocusedContainerColor = LightGreyBackground,
                    disabledContainerColor = LightGreyBackground,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    errorBorderColor = MaterialTheme.colorScheme.error
                ),
                textStyle = TextStyle(fontSize =  14.sp),
                shape = RoundedCornerShape(8.dp)
            )

            // Password Field
            Text(
                text = "Password",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 4.dp),
                textAlign = TextAlign.Start
            )
            OutlinedTextField(
                value = viewModel.password.value,
                onValueChange = viewModel::onPasswordChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 14.dp),
                placeholder = { Text("Enter your password", fontSize =  14.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisibility)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    IconButton(onClick = {
                        passwordVisibility = !passwordVisibility
                    }) {
                        Icon(imageVector = image, contentDescription = "Toggle password visibility")
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = LightGreyBackground,
                    unfocusedContainerColor = LightGreyBackground,
                    disabledContainerColor = LightGreyBackground,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    errorBorderColor = MaterialTheme.colorScheme.error
                ),
                textStyle = TextStyle(fontSize =  14.sp),
                shape = RoundedCornerShape(8.dp)
            )

            // Login Button
            Button(
                onClick = viewModel::login,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(8.dp),
                enabled = viewModel.loginResult.value !is Resource.Loading // Disable button while loading
            ) {
                if (viewModel.loginResult.value is Resource.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Login", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.navigate(Screen.Registration.route) }) {
                Text("Have an invitation code? Register")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    ResiTrackTheme {
        LoginScreen(rememberNavController())
    }
}