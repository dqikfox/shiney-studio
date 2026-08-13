package com.shadow.shineyverse.data.remote

import com.shadow.shineyverse.data.repository.SettingsRepository
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Rewrites every request's scheme/host/port to whatever server address is
 * currently configured in Settings, so changing it doesn't require rebuilding
 * Retrofit/OkHttp - Retrofit itself is only ever given a placeholder base URL.
 */
class DynamicBaseUrlInterceptor @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val configured = settingsRepository.currentBaseUrlBlocking().toHttpUrlOrNull()
            ?: return chain.proceed(original)

        val newUrl = original.url.newBuilder()
            .scheme(configured.scheme)
            .host(configured.host)
            .port(configured.port)
            .build()

        return chain.proceed(original.newBuilder().url(newUrl).build())
    }
}
