package com.shadow.shineyverse.data.remote

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Cookie

@Serializable
data class StoredCookie(
    val name: String,
    val value: String,
    val expiresAt: Long,
    val domain: String,
    val path: String,
    val secure: Boolean,
    val httpOnly: Boolean,
    val hostOnly: Boolean,
)

fun Cookie.toStored() = StoredCookie(name, value, expiresAt, domain, path, secure, httpOnly, hostOnly)

fun StoredCookie.toCookie(): Cookie {
    val builder = Cookie.Builder()
        .name(name)
        .value(value)
        .expiresAt(expiresAt)
        .path(path)
    if (hostOnly) builder.hostOnlyDomain(domain) else builder.domain(domain)
    if (secure) builder.secure()
    if (httpOnly) builder.httpOnly()
    return builder.build()
}

/**
 * Durable, encrypted storage for the session cookie set, keyed by host. Backs
 * [SessionCookieJar] - the session cookie is a live credential, so this uses
 * EncryptedSharedPreferences rather than plain DataStore.
 */
class EncryptedCookieStore(context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "ultra_cookie_store",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun loadAll(): Map<String, List<StoredCookie>> {
        val raw = prefs.getString(KEY_COOKIES, null) ?: return emptyMap()
        return try {
            json.decodeFromString(raw)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun saveAll(data: Map<String, List<StoredCookie>>) {
        prefs.edit().putString(KEY_COOKIES, json.encodeToString(data)).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_COOKIES = "cookies_by_host"
    }
}
