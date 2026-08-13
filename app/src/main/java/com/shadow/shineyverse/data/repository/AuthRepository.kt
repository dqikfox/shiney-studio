package com.shadow.shineyverse.data.repository

import com.shadow.shineyverse.data.remote.ApiResult
import com.shadow.shineyverse.data.remote.SessionCookieJar
import com.shadow.shineyverse.data.remote.api.AuthApi
import com.shadow.shineyverse.data.remote.api.AuthRedirectApi
import com.shadow.shineyverse.data.remote.safeApiCall
import java.io.IOException
import java.net.URLDecoder
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthOutcome {
    data object Success : AuthOutcome()
    data class Failure(val message: String) : AuthOutcome()
}

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val authRedirectApi: AuthRedirectApi,
    private val cookieJar: SessionCookieJar,
) {
    suspend fun login(username: String, password: String): AuthOutcome {
        return try {
            val response = authRedirectApi.login(username = username, password = password)
            val location = response.headers()["Location"]
            if (response.code() == 303 && !isRedirectToPath(location, "/login")) {
                AuthOutcome.Success
            } else {
                AuthOutcome.Failure("Invalid username or password.")
            }
        } catch (e: IOException) {
            AuthOutcome.Failure(unreachableMessage)
        }
    }

    suspend fun register(username: String, password: String, confirm: String): AuthOutcome {
        return try {
            val response = authRedirectApi.register(username = username, password = password, confirm = confirm)
            val location = response.headers()["Location"]
            if (response.code() == 303 && !isRedirectToPath(location, "/register")) {
                AuthOutcome.Success
            } else {
                AuthOutcome.Failure(extractErrorMessage(location) ?: "Couldn't register with those details.")
            }
        } catch (e: IOException) {
            AuthOutcome.Failure(unreachableMessage)
        }
    }

    /** Best-effort server-side logout; the local session is always cleared regardless. */
    suspend fun logout() {
        try {
            authApi.logout()
        } catch (e: IOException) {
            // Offline logout is still a logout from this device's point of view.
        }
        clearLocalSession()
    }

    fun clearLocalSession() {
        cookieJar.clear()
    }

    /** Cheap cold-start probe - true only if the stored session cookie is still valid. */
    suspend fun hasValidSession(): Boolean = safeApiCall { authApi.checkSession() } is ApiResult.Success

    private fun isRedirectToPath(locationHeader: String?, path: String): Boolean =
        locationHeader?.substringBefore('?') == path

    private fun extractErrorMessage(locationHeader: String?): String? {
        val encoded = locationHeader
            ?.substringAfter("error=", missingDelimiterValue = "")
            ?.substringBefore('&')
            ?.takeIf { it.isNotBlank() }
            ?: return null
        return runCatching { URLDecoder.decode(encoded, "UTF-8") }.getOrNull()
    }

    private val unreachableMessage =
        "Couldn't reach the server - check the address in Settings and that it's running."
}
