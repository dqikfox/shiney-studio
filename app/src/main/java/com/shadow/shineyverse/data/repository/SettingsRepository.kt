package com.shadow.shineyverse.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/** Turns whatever the user typed (an IP, a Tailscale hostname, with or without a scheme/port) into a usable base URL. */
fun normalizeBaseUrl(input: String): String {
    var url = input.trim().trimEnd('/')
    if (!url.startsWith("http://") && !url.startsWith("https://")) {
        url = "http://$url"
    }
    return url
}

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    /** Ships pre-configured for the one server this app actually talks to - still
     * editable in Settings, but there's nothing to set up on first launch. */
    val serverBaseUrl: Flow<String> = dataStore.data.map { it[SERVER_BASE_URL_KEY] ?: DEFAULT_SERVER_BASE_URL }

    suspend fun setServerBaseUrl(rawInput: String) {
        dataStore.edit { it[SERVER_BASE_URL_KEY] = normalizeBaseUrl(rawInput) }
    }

    /**
     * Synchronous read for use inside an OkHttp [okhttp3.Interceptor], which runs on a
     * background dispatcher already and needs the current value at request time,
     * not a reactive stream.
     */
    fun currentBaseUrlBlocking(): String = runBlocking { serverBaseUrl.first() }

    companion object {
        private val SERVER_BASE_URL_KEY = stringPreferencesKey("server_base_url")

        // Publicly funneled, so it works whether or not Tailscale is running on this device.
        const val DEFAULT_SERVER_BASE_URL = "https://shadow.tail886dbb.ts.net:8443"
    }
}
