package com.shadow.shineyverse.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shadow.shineyverse.ui.common.BrandWordmark
import com.shadow.shineyverse.ui.common.ErrorBanner
import com.shadow.shineyverse.ui.common.GlassPanel
import com.shadow.shineyverse.ui.common.GradientButton

@Composable
fun ServerUrlScreen(
    onContinue: () -> Unit,
    viewModel: ServerUrlViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BrandWordmark(fontSize = 24.sp)
            Text(
                "Connect to the PC running ShinyVerse - its LAN IP or Tailscale address, with the port.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            GlassPanel(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = state.input,
                        onValueChange = viewModel::onInputChange,
                        label = { Text("Server address") },
                        placeholder = { Text("100.x.x.x:7860") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    state.error?.let { ErrorBanner(it) }
                    GradientButton(
                        text = if (state.isSaving) "Saving..." else "Continue",
                        onClick = { viewModel.save(onContinue) },
                        enabled = !state.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
