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

data class RegisterUiState(
    val username: String = "",
    val password: String = "",
    val confirm: String = "",
    val isRegistering: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(username = value, error = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun onConfirmChange(value: String) {
        _uiState.value = _uiState.value.copy(confirm = value, error = null)
    }

    fun register(onSuccess: () -> Unit) {
        val state = _uiState.value
        val validationError = when {
            state.username.trim().length < 3 -> "Username must be at least 3 characters."
            state.password.length < 8 -> "Password must be at least 8 characters."
            state.password != state.confirm -> "Passwords don't match."
            else -> null
        }
        if (validationError != null) {
            _uiState.value = state.copy(error = validationError)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRegistering = true, error = null)
            when (val outcome = authRepository.register(state.username.trim(), state.password, state.confirm)) {
                is AuthOutcome.Success -> {
                    _uiState.value = _uiState.value.copy(isRegistering = false)
                    onSuccess()
                }
                is AuthOutcome.Failure -> {
                    _uiState.value = _uiState.value.copy(isRegistering = false, error = outcome.message)
                }
            }
        }
    }
}
