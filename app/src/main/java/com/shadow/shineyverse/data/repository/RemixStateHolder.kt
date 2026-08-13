package com.shadow.shineyverse.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Settings from a past generation, to reload into the Create screen ("Remix").
 * The `/gallery` sidecar metadata only ever exposes prompt/model/seed for a
 * past item - not the full width/steps/cfg/etc. parameter set - so that's
 * all Remix can restore here, matching the real web app's own Remix behavior.
 * Everything else falls back to that model's normal defaults.
 */
data class RemixRequest(
    val modelKey: String,
    val prompt: String,
    val seed: Long,
)

/**
 * Hand-off between the (Phase 3) item detail screen's Remix action and the
 * Create screen. Route arguments stay primitive, so this carries the actual
 * settings object instead of serializing it into the nav graph.
 */
@Singleton
class RemixStateHolder @Inject constructor() {
    private val _pending = MutableStateFlow<RemixRequest?>(null)
    val pending: StateFlow<RemixRequest?> = _pending

    fun set(request: RemixRequest) {
        _pending.value = request
    }

    /** Consumes the pending request so it's only applied once. */
    fun consume(): RemixRequest? {
        val value = _pending.value
        _pending.value = null
        return value
    }
}
