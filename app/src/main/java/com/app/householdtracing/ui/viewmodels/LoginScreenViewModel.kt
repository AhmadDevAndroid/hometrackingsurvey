package com.app.householdtracing.ui.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.householdtracing.data.model.LoginResponseBody
import com.app.householdtracing.data.repositoryImpl.AuthRepositoryImpl
import kotlinx.coroutines.launch

class LoginScreenViewModel(
    private val mApplication: Application,
    private val authRepositoryImpl: AuthRepositoryImpl
) : ViewModel() {

    var emailTextField = mutableStateOf("")
    var passwordTextField = mutableStateOf("")

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
}


sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val data: LoginResponseBody) : LoginUiState()
    data class Error(val error: String) : LoginUiState()
}