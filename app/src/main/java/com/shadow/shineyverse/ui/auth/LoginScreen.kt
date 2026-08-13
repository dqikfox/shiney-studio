package com.shadow.shineyverse.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shadow.shineyverse.ui.common.BrandWordmark
import com.shadow.shineyverse.ui.common.ErrorBanner
import com.shadow.shineyverse.ui.common.GlassPanel
import com.shadow.shineyverse.ui.common.GradientButton

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onEditServerUrl: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
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
            BrandWordmark(fontSize = 28.sp)
            Text(
                "AI image & video generation, running on your own server.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(28.dp))

            GlassPanel(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Sign in", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = state.username,
                        onValueChange = viewModel::onUsernameChange,
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    state.error?.let { ErrorBanner(it) }
                    GradientButton(
                        text = if (state.isLoggingIn) "Signing in..." else "Sign in",
                        onClick = { viewModel.login(onLoggedIn) },
                        enabled = !state.isLoggingIn,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            TextButton(onClick = onNavigateToRegister) {
                Text("Don't have an account? Register")
            }
            TextButton(onClick = onEditServerUrl) {
                Text("Change server address")
            }
        }
    }
}
