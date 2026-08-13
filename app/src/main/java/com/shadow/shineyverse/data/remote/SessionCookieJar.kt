package com.shadow.shineyverse.data.remote

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists cookies (the `shinyverse_session` cookie in practice) across app
 * restarts, keyed by host so switching server URLs in Settings can't leak a
 * cookie meant for one server into another.
 */
@Singleton
class SessionCookieJar @Inject constructor(
    private val store: EncryptedCookieStore,
) : CookieJar {

    private val lock = Any()
    private val cookiesByHost: MutableMap<String, MutableList<Cookie>> by lazy {
        store.loadAll()
            .mapValues { (_, stored) -> stored.map { it.toCookie() }.toMutableList() }
            .toMutableMap()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        synchronized(lock) {
            val existing = cookiesByHost.getOrPut(url.host) { mutableListOf() }
            for (cookie in cookies) {
                existing.removeAll { it.name == cookie.name && it.path == cookie.path }
                if (cookie.expiresAt > System.currentTimeMillis()) {
                    existing.add(cookie)
                }
            }
            persist()
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        synchronized(lock) {
            val existing = cookiesByHost[url.host] ?: return emptyList()
            val now = System.currentTimeMillis()
            val expired = existing.filter { it.expiresAt <= now }
            if (expired.isNotEmpty()) {
                existing.removeAll(expired)
                persist()
            }
            return existing.filter { it.matches(url) }
        }
    }

    /** Called on logout and on a global 401 (session-expired) event. */
    fun clear() {
        synchronized(lock) {
            cookiesByHost.clear()
            store.clear()
        }
    }

    private fun persist() {
        store.saveAll(cookiesByHost.mapValues { (_, cookies) -> cookies.map { it.toStored() } })
    }
}
