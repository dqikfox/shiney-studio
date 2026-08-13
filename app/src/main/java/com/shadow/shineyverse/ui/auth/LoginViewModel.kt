package com.shadow.shineyverse.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadow.shineyverse.data.repository.AuthOutcome
import com.shadow.shineyverse.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoggingIn: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(username = value, error = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.username.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Enter both a username and password.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoggingIn = true, error = null)
            when (val outcome = authRepository.login(state.username.trim(), state.password)) {
                is AuthOutcome.Success -> {
                    _uiState.value = _uiState.value.copy(isLoggingIn = false)
                    onSuccess()
                }
                is AuthOutcome.Failure -> {
                    _uiState.value = _uiState.value.copy(isLoggingIn = false, error = outcome.message)
                }
            }
        }
    }
}
