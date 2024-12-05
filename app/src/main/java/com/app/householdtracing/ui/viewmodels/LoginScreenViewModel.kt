package com.app.householdtracing.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.householdtracing.data.model.responsedto.LoginResponseBody
import com.app.householdtracing.data.repositoryImpl.AuthRepositoryImpl
import kotlinx.coroutines.launch

class LoginScreenViewModel(
    private val authRepositoryImpl: AuthRepositoryImpl
) : ViewModel() {

    var emailTextField by mutableStateOf("hamzaali@gmail.com")
    var passwordTextField by mutableStateOf("123456")
    var isAllPermissionGranted by mutableStateOf(false)

    var authState by mutableStateOf<LoginUiState>(LoginUiState.Idle)
        private set

    fun authenticate(identifier: String, password: String) {
        viewModelScope.launch {
            authState = LoginUiState.Loading
            val result = authRepositoryImpl.authenticate(identifier, password)
            authState = result.fold(
                onSuccess = { LoginUiState.Success(it) },
                onFailure = { LoginUiState.Error(it.localizedMessage ?: "Unknown Error") }
            )
        }
    }

    fun getUser() = authRepositoryImpl.getUser()
}


sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val data: LoginResponseBody) : LoginUiState()
    data class Error(val error: String) : LoginUiState()
}