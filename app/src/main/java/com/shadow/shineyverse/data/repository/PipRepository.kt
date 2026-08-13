package com.shadow.shineyverse.data.repository

import android.content.Context
import com.shadow.shineyverse.data.remote.ApiResult
import com.shadow.shineyverse.data.remote.dto.MODEL_KIND_IMAGE
import com.shadow.shineyverse.data.remote.dto.MutationResultDto
import com.shadow.shineyverse.service.GenerationService
import dagger.hilt.android.qualifiers.ApplicationContext
import io.elevenlabs.ClientTool
import io.elevenlabs.ClientToolResult
import io.elevenlabs.ConversationClient
import io.elevenlabs.ConversationConfig
import io.elevenlabs.ConversationSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class PipTranscriptEntry(val speaker: String, val text: String)

/**
 * Wraps the ElevenLabs Conversational AI Kotlin SDK for Pip, the Kid Mode voice buddy.
 * Every client tool here maps onto a repository method already built for the
 * Create/Gallery screens - pure reuse, not new backend-calling code. Unlike the web
 * version, this SDK has no documented file/image-upload API, so Pip can't "see" a
 * just-generated result the way the browser widget can - a known, real gap, not an
 * oversight.
 */
@Singleton
class PipRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val modelsRepository: ModelsRepository,
    private val generationRepository: GenerationRepository,
    private val galleryRepository: GalleryRepository,
) {
    private var session: ConversationSession? = null

    /** Only for firing off the SDK's suspend teardown call from non-suspend call
     * sites (ViewModel.onCleared can't launch on viewModelScope - it's already
     * cancelled by the time onCleared runs). */
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _transcript = MutableStateFlow<List<PipTranscriptEntry>>(emptyList())
    val transcript: StateFlow<List<PipTranscriptEntry>> = _transcript.asStateFlow()

    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError: StateFlow<String?> = _connectionError.asStateFlow()

    val isConnected: Boolean get() = session != null

    suspend fun startSession(agentId: String, textOnly: Boolean) {
        if (session != null) return
        _connectionError.value = null
        val config = ConversationConfig(
            agentId = agentId,
            textOnly = textOnly,
            clientTools = buildClientTools(),
            onAgentResponseEvent = { text, _ -> appendTranscript("Pip", text) },
            onUserTranscriptEvent = { text, _ -> appendTranscript("You", text) },
            onDisconnect = { session = null },
        )
        try {
            session = ConversationClient.startSession(config, appContext)
        } catch (e: Exception) {
            _connectionError.value = "Couldn't reach Pip right now - try again in a bit!"
        }
    }

    fun sendText(text: String) {
        val active = session ?: return
        appendTranscript("You", text)
        active.sendUserMessage(text)
    }

    fun endSession() {
        val active = session ?: return
        session = null
        repositoryScope.launch { active.endSession() }
    }

    fun clearTranscript() {
        _transcript.value = emptyList()
    }

    private fun appendTranscript(speaker: String, text: String) {
        _transcript.value = _transcript.value + PipTranscriptEntry(speaker, text)
    }

    private fun buildClientTools(): Map<String, ClientTool> = mapOf(
        "generate_creation" to tool { params ->
            val prompt = params["prompt"] as? String
            val kind = params["kind"] as? String ?: MODEL_KIND_IMAGE
            if (prompt.isNullOrBlank()) ClientToolResult.failure("Missing prompt") else generateCreation(prompt, kind)
        },
        "list_gallery" to tool { params -> listGallery(params["folder"] as? String ?: "") },
        "list_folders" to tool { listFolders() },
        "create_folder" to tool { params ->
            val name = params["name"] as? String
            if (name.isNullOrBlank()) {
                ClientToolResult.failure("Missing name")
            } else {
                mutationResult(galleryRepository.createFolder(name), "Created the album \"$name\".")
            }
        },
        "move_to_folder" to tool { params -> moveOrCopy(params, isCopy = false) },
        "copy_to_folder" to tool { params -> moveOrCopy(params, isCopy = true) },
        "delete_item" to tool { params ->
            val fileName = params["file_name"] as? String
            val kind = params["kind"] as? String ?: "image"
            if (fileName.isNullOrBlank()) {
                ClientToolResult.failure("Missing file_name")
            } else {
                mutationResult(galleryRepository.deleteItem(fileName, kind), "Deleted $fileName.")
            }
        },
    )

    private fun tool(block: suspend (Map<String, Any>) -> ClientToolResult): ClientTool =
        object : ClientTool {
            override suspend fun execute(parameters: Map<String, Any>): ClientToolResult? = block(parameters)
        }

    private suspend fun generateCreation(prompt: String, kind: String): ClientToolResult {
        val modelsResult = modelsRepository.fetchModels()
        if (modelsResult !is ApiResult.Success) return ClientToolResult.failure("Couldn't load models.")
        val entry = modelsResult.data.entries.firstOrNull { it.value.kind == kind }
            ?: return ClientToolResult.failure("No $kind model available.")
        val (modelKey, info) = entry.key to entry.value

        val request = GenerationRequest(
            modelKey = modelKey,
            isVideo = kind != MODEL_KIND_IMAGE,
            prompt = prompt,
            negative = "",
            width = info.width,
            height = info.height,
            steps = info.steps,
            cfg = info.cfg,
            sampler = info.sampler,
            scheduler = info.scheduler,
            length = info.length ?: 121,
            fps = info.fps ?: 24,
            imageFilename = "",
            folder = "",
            count = 1,
            randomizeSeed = true,
            baseSeed = 0L,
        )
        generationRepository.reset()
        appContext.startForegroundService(GenerationService.buildIntent(appContext, request, info.label))
        return ClientToolResult.success("Making it now - I'll let you know when it's ready!")
    }

    private suspend fun listGallery(folder: String): ClientToolResult {
        return when (val result = galleryRepository.fetchGallery(folder, limit = 20, offset = 0)) {
            is ApiResult.Success -> {
                val items = result.data.items
                if (items.isEmpty()) {
                    ClientToolResult.success("No items found.")
                } else {
                    val lines = items.take(10).joinToString("\n") { "- ${it.name} (${it.kind}): \"${it.prompt.orEmpty()}\"" }
                    ClientToolResult.success("Found ${items.size} item(s). Most recent first:\n$lines")
                }
            }
            else -> ClientToolResult.failure("Couldn't load the gallery.")
        }
    }

    private suspend fun listFolders(): ClientToolResult {
        return when (val result = galleryRepository.fetchFolders()) {
            is ApiResult.Success -> {
                if (result.data.isEmpty()) {
                    ClientToolResult.success("No albums exist yet.")
                } else {
                    ClientToolResult.success(result.data.joinToString("\n") { "${it.name} (${it.count} items)" })
                }
            }
            else -> ClientToolResult.failure("Couldn't load albums.")
        }
    }

    private suspend fun moveOrCopy(params: Map<String, Any>, isCopy: Boolean): ClientToolResult {
        val fileName = params["file_name"] as? String ?: return ClientToolResult.failure("Missing file_name")
        val folder = params["folder"] as? String ?: return ClientToolResult.failure("Missing folder")
        val kind = params["kind"] as? String ?: "image"
        val result = if (isCopy) {
            galleryRepository.copyItem(fileName, kind, folder)
        } else {
            galleryRepository.moveItem(fileName, kind, folder)
        }
        val successMessage = if (isCopy) "Copied $fileName into \"$folder\"." else "Moved $fileName into \"$folder\"."
        return mutationResult(result, successMessage)
    }

    private fun mutationResult(result: ApiResult<MutationResultDto>, successMessage: String): ClientToolResult =
        when (result) {
            is ApiResult.Success -> if (result.data.ok) {
                ClientToolResult.success(successMessage)
            } else {
                ClientToolResult.failure(result.data.error ?: "That didn't work.")
            }
            else -> ClientToolResult.failure("Couldn't reach the server.")
        }
}
