package com.shadow.shineyverse.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadow.shineyverse.data.repository.AuthRepository
import com.shadow.shineyverse.data.repository.SessionManager
import com.shadow.shineyverse.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StartupViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    sessionManager: SessionManager,
) : ViewModel() {

    sealed class StartDestination {
        data object Loading : StartDestination()
        data object ServerSetup : StartDestination()
        data object Login : StartDestination()
        data object Home : StartDestination()
    }

    private val _startDestination = MutableStateFlow<StartDestination>(StartDestination.Loading)
    val startDestination: StateFlow<StartDestination> = _startDestination

    /** Collected once by the nav graph to force navigation back to Login on any global 401. */
    val sessionExpired: SharedFlow<Unit> = sessionManager.sessionExpired

    init {
        viewModelScope.launch {
            val baseUrl = settingsRepository.serverBaseUrl.first()
            _startDestination.value = when {
                baseUrl.isBlank() -> StartDestination.ServerSetup
                authRepository.hasValidSession() -> StartDestination.Home
                else -> StartDestination.Login
            }
        }
    }

    fun onSessionExpiredHandled() {
        authRepository.clearLocalSession()
    }
}
