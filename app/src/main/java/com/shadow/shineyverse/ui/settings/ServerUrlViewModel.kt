package com.shadow.shineyverse.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadow.shineyverse.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ServerUrlUiState(
    val input: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ServerUrlViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServerUrlUiState())
    val uiState: StateFlow<ServerUrlUiState> = _uiState

    init {
        viewModelScope.launch {
            val current = settingsRepository.serverBaseUrl.first()
            if (current.isNotBlank()) {
                _uiState.value = _uiState.value.copy(input = current)
            }
        }
    }

    fun onInputChange(value: String) {
        _uiState.value = _uiState.value.copy(input = value, error = null)
    }

    fun save(onSaved: () -> Unit) {
        val value = _uiState.value.input.trim()
        if (value.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Enter your server's address, e.g. 100.x.x.x:7860")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            settingsRepository.setServerBaseUrl(value)
            _uiState.value = _uiState.value.copy(isSaving = false)
            onSaved()
        }
    }
}
