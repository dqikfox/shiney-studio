package com.shadow.shineyverse.ui.generate

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shadow.shineyverse.data.remote.dto.ModelInfoDto
import com.shadow.shineyverse.data.repository.GenerationSessionState
import com.shadow.shineyverse.ui.common.ErrorBanner
import com.shadow.shineyverse.ui.common.FullScreenLoading
import com.shadow.shineyverse.ui.common.GlassPanel
import com.shadow.shineyverse.ui.common.GradientButton

@Composable
fun GenerateScreen(viewModel: GenerateViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val generationState by viewModel.generationState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@rememberLauncherForActivityResult
        val name = uri.lastPathSegment ?: "photo.jpg"
        viewModel.onImagePicked(bytes, name)
    }

    Scaffold { padding ->
        if (state.isLoadingModels) {
            FullScreenLoading(modifier = Modifier.fillMaxSize().padding(padding))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    "Describe what you want, pick a style, and it generates on your own server.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            state.modelsError?.let { item { ErrorBanner(it) } }
            state.generalError?.let { item { ErrorBanner(it) } }

            item {
                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        ModelDropdown(
                            models = state.models,
                            selectedKey = state.selectedModelKey,
                            onSelect = viewModel::selectModel,
                        )
                        OutlinedTextField(
                            value = state.prompt,
                            onValueChange = viewModel::onPromptChange,
                            label = { Text("What should I make?") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(STARTER_PROMPTS) { (label, _) ->
                                StarterChip(label = label, onClick = { viewModel.onStarterPicked(label) })
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = viewModel::surpriseMe) { Text("Surprise Me!") }
                            OutlinedButton(onClick = viewModel::randomPokemon) { Text("Random Pokemon! (1 in 50 Shiny!)") }
                        }

                        if (state.needsImageUpload) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { imagePicker.launch("image/*") }) {
                                    Text("Choose a starting photo")
                                }
                                when {
                                    state.isUploadingImage -> Text("Uploading...", style = MaterialTheme.typography.bodyMedium)
                                    state.uploadedImageFilename.isNotBlank() -> Text(
                                        "Ready: ${state.pickedImageName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    else -> Text("No photo chosen yet.", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("How many at once?")
                            BatchStepper(value = state.batchCount, onChange = viewModel::onBatchCountChange)
                        }
                    }
                }
            }

            item {
                TextButton(onClick = viewModel::onToggleAdvanced) {
                    Text(if (state.advancedExpanded) "Hide grown-up settings" else "Grown-up settings")
                }
            }
            if (state.advancedExpanded) {
                item {
                    GlassPanel(modifier = Modifier.fillMaxWidth()) {
                        AdvancedSettingsSection(
                            state = state,
                            onNegativeChange = viewModel::onNegativeChange,
                            onWidthChange = viewModel::onWidthChange,
                            onHeightChange = viewModel::onHeightChange,
                            onStepsChange = viewModel::onStepsChange,
                            onCfgChange = viewModel::onCfgChange,
                            onSamplerChange = viewModel::onSamplerChange,
                            onSchedulerChange = viewModel::onSchedulerChange,
                            onLengthChange = viewModel::onLengthChange,
                            onFpsChange = viewModel::onFpsChange,
                            onSeedChange = viewModel::onSeedChange,
                            onRandomizeToggle = viewModel::onRandomizeToggle,
                        )
                    }
                }
            }

            item {
                GradientButton(
                    text = "Generate",
                    onClick = viewModel::startGeneration,
                    enabled = state.canGenerate && generationState !is GenerationSessionState.Running,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                GenerationProgress(generationState, pokemonReveal = state.pendingPokemonReveal)
            }
        }
    }
}

@Composable
private fun StarterChip(label: String, onClick: () -> Unit) {
    AssistChip(onClick = onClick, label = { Text(label) })
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun ModelDropdown(
    models: Map<String, ModelInfoDto>,
    selectedKey: String,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = models[selectedKey]

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.label.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Quality") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            models.forEach { (key, info) ->
                DropdownMenuItem(
                    text = { Text("${info.label} (~${info.etaSeconds}s)") },
                    onClick = {
                        onSelect(key)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun BatchStepper(value: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = { onChange(value - 1) }, enabled = value > 1) { Text("-") }
        Text(value.toString(), style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = { onChange(value + 1) }, enabled = value < 4) { Text("+") }
    }
}

@Composable
private fun GenerationProgress(state: GenerationSessionState, pokemonReveal: PokemonReveal?) {
    when (state) {
        GenerationSessionState.Idle -> {}
        is GenerationSessionState.Running -> Text(
            "Making ${if (state.isVideo) "movie" else "picture"} ${state.index} of ${state.total}... ${state.elapsedSeconds}s elapsed",
            style = MaterialTheme.typography.bodyMedium,
        )
        is GenerationSessionState.ItemCompleted -> Text(
            "Finished ${state.index} of ${state.total}.",
            style = MaterialTheme.typography.bodyMedium,
        )
        is GenerationSessionState.BatchCompleted -> {
            if (pokemonReveal != null) {
                Text(
                    if (pokemonReveal.shiny) {
                        "SHINY ${pokemonReveal.name.uppercase()}! You got insanely lucky!!"
                    } else {
                        "You got a ${pokemonReveal.name}!"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (pokemonReveal.shiny) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                )
            } else {
                Text(
                    "All done - ${state.results.size} created! Check the Gallery tab.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        is GenerationSessionState.Error -> ErrorBanner(state.message)
    }
}
