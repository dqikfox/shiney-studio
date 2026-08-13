package com.shadow.shineyverse.ui.pip

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shadow.shineyverse.data.repository.PipTranscriptEntry
import com.shadow.shineyverse.ui.common.ErrorBanner

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PipScreen(onBack: () -> Unit, viewModel: PipViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val micPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.startVoice()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pip") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.error?.let { ErrorBanner(it) }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(state.transcript) { entry -> TranscriptLine(entry) }
            }

            if (state.isConnected) {
                Text("Listening/talking - voice session active.", style = MaterialTheme.typography.labelSmall)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    enabled = !state.isConnecting && !state.isConnected,
                ) {
                    Text(if (state.isConnecting) "Connecting..." else "Talk to Pip!")
                }
                if (state.isConnected) {
                    Button(onClick = { viewModel.disconnect() }) { Text("Stop") }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.textInput,
                    onValueChange = viewModel::onTextInputChange,
                    label = { Text("Type to Pip...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = { viewModel.sendText() }) { Text("Send") }
            }
        }
    }
}

@Composable
private fun TranscriptLine(entry: PipTranscriptEntry) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("${entry.speaker}:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Text(entry.text, style = MaterialTheme.typography.bodyMedium)
    }
}
