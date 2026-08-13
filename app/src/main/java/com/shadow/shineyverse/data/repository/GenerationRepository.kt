package com.shadow.shineyverse.data.repository

import com.shadow.shineyverse.data.remote.ApiResult
import com.shadow.shineyverse.data.remote.api.GenerationApi
import com.shadow.shineyverse.data.remote.dto.GenerateResponseDto
import com.shadow.shineyverse.data.remote.dto.UploadImageResponseDto
import com.shadow.shineyverse.data.remote.errorMessage
import com.shadow.shineyverse.data.remote.safeApiCall
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

data class GenerationRequest(
    val modelKey: String,
    val isVideo: Boolean,
    val prompt: String,
    val negative: String,
    val width: Int,
    val height: Int,
    val steps: Int,
    val cfg: Float,
    val sampler: String,
    val scheduler: String,
    val length: Int,
    val fps: Int,
    val imageFilename: String,
    val folder: String,
    val count: Int,
    val randomizeSeed: Boolean,
    val baseSeed: Long,
)

sealed class GenerationSessionState {
    data object Idle : GenerationSessionState()
    data class Running(
        val index: Int,
        val total: Int,
        val modelLabel: String,
        val elapsedSeconds: Int,
        val isVideo: Boolean,
    ) : GenerationSessionState()
    data class ItemCompleted(val index: Int, val total: Int, val result: GenerateResponseDto) : GenerationSessionState()
    data class BatchCompleted(val results: List<GenerateResponseDto>) : GenerationSessionState()
    data class Error(val message: String, val completedResults: List<GenerateResponseDto>) : GenerationSessionState()
}

/**
 * Owns the actual generation batch loop. Runs inside [com.shadow.shineyverse.service.GenerationService]'s
 * scope so it survives navigation/rotation/backgrounding - the UI only ever observes [state].
 */
@Singleton
class GenerationRepository @Inject constructor(
    private val generationApi: GenerationApi,
) {
    private val _state = MutableStateFlow<GenerationSessionState>(GenerationSessionState.Idle)
    val state: StateFlow<GenerationSessionState> = _state

    suspend fun uploadImage(bytes: ByteArray, filename: String): ApiResult<UploadImageResponseDto> {
        val body = bytes.toRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", filename, body)
        return safeApiCall { generationApi.uploadImage(part) }
    }

    suspend fun runBatch(request: GenerationRequest, modelLabel: String) {
        val results = mutableListOf<GenerateResponseDto>()

        for (index in 0 until request.count) {
            val seed = if (request.randomizeSeed) Random.nextLong(0, 1_000_000_000_000L) else request.baseSeed
            val itemStartMillis = System.currentTimeMillis()
            _state.value = GenerationSessionState.Running(index + 1, request.count, modelLabel, 0, request.isVideo)

            val result = coroutineScope {
                val ticker = launch {
                    while (isActive) {
                        delay(1000)
                        val elapsed = ((System.currentTimeMillis() - itemStartMillis) / 1000).toInt()
                        _state.value = GenerationSessionState.Running(index + 1, request.count, modelLabel, elapsed, request.isVideo)
                    }
                }
                val apiResult = safeApiCall {
                    generationApi.generate(
                        model = request.modelKey,
                        prompt = request.prompt,
                        negative = request.negative,
                        width = request.width,
                        height = request.height,
                        steps = request.steps,
                        cfg = request.cfg,
                        seed = seed,
                        sampler = request.sampler,
                        scheduler = request.scheduler,
                        length = request.length,
                        fps = request.fps,
                        imageFilename = request.imageFilename,
                        folder = request.folder,
                    )
                }
                ticker.cancel()
                apiResult
            }

            when (result) {
                is ApiResult.Success -> {
                    val body = result.data
                    if (!body.ok) {
                        _state.value = GenerationSessionState.Error(body.error ?: "Generation failed.", results.toList())
                        return
                    }
                    results.add(body)
                    _state.value = GenerationSessionState.ItemCompleted(index + 1, request.count, body)
                }
                else -> {
                    _state.value = GenerationSessionState.Error(result.errorMessage(), results.toList())
                    return
                }
            }
        }

        _state.value = GenerationSessionState.BatchCompleted(results.toList())
    }

    fun reset() {
        _state.value = GenerationSessionState.Idle
    }
}
