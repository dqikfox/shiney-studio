package com.shadow.shineyverse.data.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Emits a global event whenever any API call comes back 401, so the UI layer
 * can react in one place (clear the cookie jar, navigate to login) instead of
 * every screen having to check for it individually.
 */
@Singleton
class SessionManager @Inject constructor() {
    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

    fun notifySessionExpired() {
        _sessionExpired.tryEmit(Unit)
    }
}
