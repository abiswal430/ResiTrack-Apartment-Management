package com.example.resitrack.features.auth

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.resitrack.data.manager.AuthManager
import com.example.resitrack.data.manager.NotificationsManager
import com.example.resitrack.util.Resource
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel(private val authManager: AuthManager, private val notificationsManager: NotificationsManager) : ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _loginResult = mutableStateOf<Resource<String>>(Resource.Success(""))
    val loginResult: State<Resource<String>> = _loginResult

    fun onEmailChange(newValue: String) {
        _email.value = newValue
    }

    fun onPasswordChange(newValue: String) {
        _password.value = newValue
    }

    fun login() {
        _loginResult.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val uid = authManager.loginUser(email.value, password.value)

                // Step 2 (NEW): Get the current device's FCM token
                val token = FirebaseMessaging.getInstance().token.await()
//                Log.d("FCM_TOKEN", "Token fetched on login: $token")

                // Step 3 (NEW): Save the new token to Firestore for this user
                notificationsManager.saveUserToken(uid, token)

                _loginResult.value = Resource.Success(uid)
            } catch (e: Exception) {
                _loginResult.value = Resource.Error(e.localizedMessage ?: "An unexpected error occurred.")
            }
        }
    }

    fun resetLoginResult() {
        _loginResult.value = Resource.Success("")
    }
}

class LoginViewModelFactory(private val authManager: AuthManager, private  val notificationsManager: NotificationsManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(authManager, notificationsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}