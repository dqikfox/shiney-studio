package com.shadow.shineyverse.ui.generate

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadow.shineyverse.data.remote.ApiResult
import com.shadow.shineyverse.data.remote.dto.MODEL_KIND_IMAGE
import com.shadow.shineyverse.data.repository.GenerationRepository
import com.shadow.shineyverse.data.repository.GenerationRequest
import com.shadow.shineyverse.data.repository.GenerationSessionState
import com.shadow.shineyverse.data.repository.ModelsRepository
import com.shadow.shineyverse.data.repository.RemixStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GenerateViewModel @Inject constructor(
    private val modelsRepository: ModelsRepository,
    private val generationRepository: GenerationRepository,
    private val remixStateHolder: RemixStateHolder,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GenerateUiState())
    val uiState: StateFlow<GenerateUiState> = _uiState

    val generationState: StateFlow<GenerationSessionState> = generationRepository.state

    init {
        viewModelScope.launch {
            loadModels()
            // Started only after models are loaded (applyRemix needs them), but runs for
            // the rest of this ViewModel's life - so a Remix triggered later, after this
            // screen already existed, still lands correctly, not just one at startup.
            remixStateHolder.pending.collect { remix ->
                if (remix != null) {
                    applyRemix(remix)
                    remixStateHolder.consume()
                }
            }
        }
    }

    private suspend fun loadModels() {
        _uiState.value = _uiState.value.copy(isLoadingModels = true, modelsError = null)
        when (val result = modelsRepository.fetchModels()) {
            is ApiResult.Success -> {
                _uiState.value = _uiState.value.copy(models = result.data, isLoadingModels = false)
                val pendingRemix = remixStateHolder.pending.value
                val defaultKey = pendingRemix?.modelKey
                    ?: result.data.entries.firstOrNull { it.value.kind == MODEL_KIND_IMAGE }?.key
                    ?: result.data.keys.firstOrNull()
                if (pendingRemix == null && defaultKey != null) {
                    selectModel(defaultKey)
                }
            }
            else -> {
                _uiState.value = _uiState.value.copy(
                    isLoadingModels = false,
                    modelsError = "Couldn't load models - check your server address in Settings.",
                )
            }
        }
    }

    private fun applyRemix(remix: com.shadow.shineyverse.data.repository.RemixRequest) {
        val info = _uiState.value.models[remix.modelKey]
        if (info == null) {
            _uiState.value = _uiState.value.copy(generalError = "That item's model isn't available on this server anymore.")
            return
        }
        // Only prompt/model/seed survive from a past item (that's all the gallery
        // metadata carries) - everything else takes this model's normal defaults.
        selectModel(remix.modelKey)
        _uiState.value = _uiState.value.copy(
            prompt = remix.prompt,
            seed = remix.seed,
            randomizeSeed = false,
        )
    }

    fun selectModel(key: String) {
        val info = _uiState.value.models[key] ?: return
        _uiState.value = _uiState.value.copy(
            selectedModelKey = key,
            width = info.width,
            height = info.height,
            steps = info.steps,
            cfg = info.cfg,
            sampler = info.sampler,
            scheduler = info.scheduler,
            length = info.length ?: _uiState.value.length,
            fps = info.fps ?: _uiState.value.fps,
            pickedImageName = null,
            uploadedImageFilename = "",
        )
    }

    fun onPromptChange(value: String) {
        _uiState.value = _uiState.value.copy(prompt = value)
    }

    fun onNegativeChange(value: String) {
        _uiState.value = _uiState.value.copy(negative = value)
    }

    fun onWidthChange(value: Int) = update { it.copy(width = value) }
    fun onHeightChange(value: Int) = update { it.copy(height = value) }
    fun onStepsChange(value: Int) = update { it.copy(steps = value) }
    fun onCfgChange(value: Float) = update { it.copy(cfg = value) }
    fun onSamplerChange(value: String) = update { it.copy(sampler = value) }
    fun onSchedulerChange(value: String) = update { it.copy(scheduler = value) }
    fun onLengthChange(value: Int) = update { it.copy(length = value) }
    fun onFpsChange(value: Int) = update { it.copy(fps = value) }
    fun onSeedChange(value: Long) = update { it.copy(seed = value) }
    fun onRandomizeToggle(value: Boolean) = update { it.copy(randomizeSeed = value) }
    fun onBatchCountChange(value: Int) = update { it.copy(batchCount = value.coerceIn(1, 4)) }
    fun onFolderChange(value: String) = update { it.copy(folder = value) }
    fun onToggleAdvanced() = update { it.copy(advancedExpanded = !it.advancedExpanded) }

    fun onImagePicked(bytes: ByteArray, displayName: String) {
        _uiState.value = _uiState.value.copy(pickedImageName = displayName, isUploadingImage = true, uploadedImageFilename = "")
        viewModelScope.launch {
            when (val result = generationRepository.uploadImage(bytes, displayName)) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(
                    isUploadingImage = false,
                    uploadedImageFilename = result.data.filename,
                )
                else -> _uiState.value = _uiState.value.copy(
                    isUploadingImage = false,
                    generalError = "Couldn't upload that photo - try again.",
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(generalError = null)
    }

    /** Fills the prompt with a random starter idea - matching the web app's "Surprise Me!"
     * button. Only fills the field; the kid still taps Generate themselves. */
    fun surpriseMe() {
        val (_, prompt) = STARTER_PROMPTS.random()
        _uiState.value = _uiState.value.copy(prompt = prompt)
    }

    fun onStarterPicked(label: String) {
        val prompt = STARTER_PROMPTS.firstOrNull { it.first == label }?.second ?: return
        _uiState.value = _uiState.value.copy(prompt = prompt)
    }

    /** Matches the web app's "Random Pokemon!" button: picks a random name, a 1-in-50
     * chance of a "shiny" variant, fills the prompt, and generates immediately. */
    fun randomPokemon() {
        val name = POKEMON_NAMES.random()
        val isShiny = (0 until 50).random() == 0
        val prompt = if (isShiny) {
            "official Pokemon anime style artwork of a rare SHINY $name, shimmering alternate-colored, sparkling magical glow, vibrant unusual colors, cute, highly detailed"
        } else {
            "official Pokemon anime style artwork of $name, vibrant colors, cute, highly detailed"
        }
        _uiState.value = _uiState.value.copy(prompt = prompt)
        startGeneration()
        _uiState.value = _uiState.value.copy(pendingPokemonReveal = PokemonReveal(name, isShiny))
    }

    fun startGeneration() {
        val state = _uiState.value
        val model = state.selectedModel ?: return
        if (!state.canGenerate) return

        val request = GenerationRequest(
            modelKey = state.selectedModelKey,
            isVideo = state.isVideoModel,
            prompt = state.prompt,
            negative = state.negative,
            width = state.width,
            height = state.height,
            steps = state.steps,
            cfg = state.cfg,
            sampler = state.sampler,
            scheduler = state.scheduler,
            length = state.length,
            fps = state.fps,
            imageFilename = state.uploadedImageFilename,
            folder = state.folder,
            count = state.batchCount,
            randomizeSeed = state.randomizeSeed,
            baseSeed = state.seed,
        )

        // Any earlier Pokemon reveal only applies to the run that set it - clear it here so
        // it doesn't linger and get misattributed to an unrelated generation.
        _uiState.value = _uiState.value.copy(pendingPokemonReveal = null)
        generationRepository.reset()
        val intent = com.shadow.shineyverse.service.GenerationService.buildIntent(appContext, request, model.label)
        appContext.startForegroundService(intent)
    }

    fun dismissGenerationResult() {
        generationRepository.reset()
    }

    private inline fun update(block: (GenerateUiState) -> GenerateUiState) {
        _uiState.value = block(_uiState.value)
    }
}
