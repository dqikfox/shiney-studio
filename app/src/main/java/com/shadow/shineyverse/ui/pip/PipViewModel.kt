package com.shadow.shineyverse.ui.pip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadow.shineyverse.data.repository.PipRepository
import com.shadow.shineyverse.data.repository.PipTranscriptEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PipUiState(
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val transcript: List<PipTranscriptEntry> = emptyList(),
    val textInput: String = "",
    val error: String? = null,
)

@HiltViewModel
class PipViewModel @Inject constructor(
    private val pipRepository: PipRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PipUiState())
    val uiState: StateFlow<PipUiState> = _uiState

    init {
        viewModelScope.launch {
            pipRepository.transcript.collect { entries ->
                _uiState.value = _uiState.value.copy(transcript = entries)
            }
        }
        viewModelScope.launch {
            pipRepository.connectionError.collect { error ->
                if (error != null) {
                    _uiState.value = _uiState.value.copy(error = error, isConnecting = false, isConnected = false)
                }
            }
        }
    }

    fun startVoice() {
        _uiState.value = _uiState.value.copy(isConnecting = true, error = null)
        viewModelScope.launch {
            pipRepository.startSession(PIP_AGENT_ID, textOnly = false)
            _uiState.value = _uiState.value.copy(isConnecting = false, isConnected = pipRepository.isConnected)
        }
    }

    fun onTextInputChange(value: String) {
        _uiState.value = _uiState.value.copy(textInput = value)
    }

    fun sendText() {
        val text = _uiState.value.textInput.trim()
        if (text.isBlank()) return
        _uiState.value = _uiState.value.copy(textInput = "")
        viewModelScope.launch {
            if (!pipRepository.isConnected) {
                _uiState.value = _uiState.value.copy(isConnecting = true, error = null)
                pipRepository.startSession(PIP_AGENT_ID, textOnly = true)
                _uiState.value = _uiState.value.copy(isConnecting = false, isConnected = pipRepository.isConnected)
            }
            pipRepository.sendText(text)
        }
    }

    fun disconnect() {
        pipRepository.endSession()
        _uiState.value = _uiState.value.copy(isConnected = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        pipRepository.endSession()
        super.onCleared()
    }

    companion object {
        // The same Pip agent already configured on ElevenLabs for the Kid Mode web app.
        const val PIP_AGENT_ID = "agent_6201kyrsh6zzfw0ar62e03g9157h"
    }
}
