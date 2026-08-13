package com.shadow.shineyverse.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.shadow.shineyverse.data.repository.GenerationRepository
import com.shadow.shineyverse.data.repository.GenerationRequest
import com.shadow.shineyverse.data.repository.GenerationSessionState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Runs the (potentially many-minute) generation batch loop as a foreground
 * service so it survives navigation, rotation, and backgrounding. If the
 * process is killed outright, the server is still generating regardless -
 * there's nothing here to persist/resume, only to reconcile against
 * `GET /queue` on next launch (a Phase 4 concern).
 */
@AndroidEntryPoint
class GenerationService : Service() {

    @Inject lateinit var generationRepository: GenerationRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var batchJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        GenerationNotifications.ensureChannel(this)
        generationRepository.state.onEach(::updateNotification).launchIn(serviceScope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(GenerationNotifications.NOTIFICATION_ID, GenerationNotifications.progress(this, "Starting..."))

        val request = intent?.toGenerationRequest()
        val modelLabel = intent?.getStringExtra(EXTRA_MODEL_LABEL).orEmpty()
        if (request != null && batchJob?.isActive != true) {
            batchJob = serviceScope.launch {
                generationRepository.runBatch(request, modelLabel)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun updateNotification(state: GenerationSessionState) {
        val text = when (state) {
            is GenerationSessionState.Running ->
                "Making ${if (state.isVideo) "movie" else "picture"} ${state.index} of ${state.total}... ${state.elapsedSeconds}s"
            is GenerationSessionState.ItemCompleted -> "Finished ${state.index} of ${state.total}"
            is GenerationSessionState.BatchCompleted -> "Done - ${state.results.size} created"
            is GenerationSessionState.Error -> "Something went wrong: ${state.message}"
            GenerationSessionState.Idle -> return
        }
        val finished = state is GenerationSessionState.BatchCompleted || state is GenerationSessionState.Error
        val notification = if (finished) GenerationNotifications.done(this, text) else GenerationNotifications.progress(this, text)
        getSystemService(NotificationManager::class.java)?.notify(GenerationNotifications.NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_MODEL_KEY = "model_key"
        private const val EXTRA_MODEL_LABEL = "model_label"
        private const val EXTRA_IS_VIDEO = "is_video"
        private const val EXTRA_PROMPT = "prompt"
        private const val EXTRA_NEGATIVE = "negative"
        private const val EXTRA_WIDTH = "width"
        private const val EXTRA_HEIGHT = "height"
        private const val EXTRA_STEPS = "steps"
        private const val EXTRA_CFG = "cfg"
        private const val EXTRA_SAMPLER = "sampler"
        private const val EXTRA_SCHEDULER = "scheduler"
        private const val EXTRA_LENGTH = "length"
        private const val EXTRA_FPS = "fps"
        private const val EXTRA_IMAGE_FILENAME = "image_filename"
        private const val EXTRA_FOLDER = "folder"
        private const val EXTRA_COUNT = "count"
        private const val EXTRA_RANDOMIZE = "randomize"
        private const val EXTRA_BASE_SEED = "base_seed"

        fun buildIntent(context: Context, request: GenerationRequest, modelLabel: String): Intent =
            Intent(context, GenerationService::class.java).apply {
                putExtra(EXTRA_MODEL_KEY, request.modelKey)
                putExtra(EXTRA_MODEL_LABEL, modelLabel)
                putExtra(EXTRA_IS_VIDEO, request.isVideo)
                putExtra(EXTRA_PROMPT, request.prompt)
                putExtra(EXTRA_NEGATIVE, request.negative)
                putExtra(EXTRA_WIDTH, request.width)
                putExtra(EXTRA_HEIGHT, request.height)
                putExtra(EXTRA_STEPS, request.steps)
                putExtra(EXTRA_CFG, request.cfg)
                putExtra(EXTRA_SAMPLER, request.sampler)
                putExtra(EXTRA_SCHEDULER, request.scheduler)
                putExtra(EXTRA_LENGTH, request.length)
                putExtra(EXTRA_FPS, request.fps)
                putExtra(EXTRA_IMAGE_FILENAME, request.imageFilename)
                putExtra(EXTRA_FOLDER, request.folder)
                putExtra(EXTRA_COUNT, request.count)
                putExtra(EXTRA_RANDOMIZE, request.randomizeSeed)
                putExtra(EXTRA_BASE_SEED, request.baseSeed)
            }

        private fun Intent.toGenerationRequest(): GenerationRequest? {
            val modelKey = getStringExtra(EXTRA_MODEL_KEY) ?: return null
            return GenerationRequest(
                modelKey = modelKey,
                isVideo = getBooleanExtra(EXTRA_IS_VIDEO, false),
                prompt = getStringExtra(EXTRA_PROMPT).orEmpty(),
                negative = getStringExtra(EXTRA_NEGATIVE).orEmpty(),
                width = getIntExtra(EXTRA_WIDTH, 512),
                height = getIntExtra(EXTRA_HEIGHT, 512),
                steps = getIntExtra(EXTRA_STEPS, 20),
                cfg = getFloatExtra(EXTRA_CFG, 8.0f),
                sampler = getStringExtra(EXTRA_SAMPLER).orEmpty().ifEmpty { "euler" },
                scheduler = getStringExtra(EXTRA_SCHEDULER).orEmpty().ifEmpty { "normal" },
                length = getIntExtra(EXTRA_LENGTH, 121),
                fps = getIntExtra(EXTRA_FPS, 24),
                imageFilename = getStringExtra(EXTRA_IMAGE_FILENAME).orEmpty(),
                folder = getStringExtra(EXTRA_FOLDER).orEmpty(),
                count = getIntExtra(EXTRA_COUNT, 1),
                randomizeSeed = getBooleanExtra(EXTRA_RANDOMIZE, true),
                baseSeed = getLongExtra(EXTRA_BASE_SEED, 0L),
            )
        }
    }
}
