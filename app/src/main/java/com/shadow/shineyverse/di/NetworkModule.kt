package com.shadow.shineyverse.di

import android.content.Context
import coil.ImageLoader
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.shadow.shineyverse.data.remote.DynamicBaseUrlInterceptor
import com.shadow.shineyverse.data.remote.PLACEHOLDER_BASE_URL
import com.shadow.shineyverse.data.remote.SessionCookieJar
import com.shadow.shineyverse.data.remote.SessionExpiryInterceptor
import com.shadow.shineyverse.data.remote.api.AuthApi
import com.shadow.shineyverse.data.remote.api.AuthRedirectApi
import com.shadow.shineyverse.data.remote.api.GalleryApi
import com.shadow.shineyverse.data.remote.api.GenerationApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import androidx.media3.datasource.okhttp.OkHttpDataSource
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }

    @Provides
    @Singleton
    @NormalClient
    fun provideNormalOkHttpClient(
        cookieJar: SessionCookieJar,
        dynamicBaseUrlInterceptor: DynamicBaseUrlInterceptor,
        sessionExpiryInterceptor: SessionExpiryInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .addInterceptor(dynamicBaseUrlInterceptor)
        .addInterceptor(sessionExpiryInterceptor)
        .addInterceptor(loggingInterceptor)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @NoRedirectClient
    fun provideNoRedirectOkHttpClient(
        cookieJar: SessionCookieJar,
        dynamicBaseUrlInterceptor: DynamicBaseUrlInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .addInterceptor(dynamicBaseUrlInterceptor)
        .addInterceptor(loggingInterceptor)
        .followRedirects(false)
        .followSslRedirects(false)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @LongTimeoutClient
    fun provideLongTimeoutOkHttpClient(
        cookieJar: SessionCookieJar,
        dynamicBaseUrlInterceptor: DynamicBaseUrlInterceptor,
        sessionExpiryInterceptor: SessionExpiryInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .addInterceptor(dynamicBaseUrlInterceptor)
        .addInterceptor(sessionExpiryInterceptor)
        .addInterceptor(loggingInterceptor)
        .callTimeout(35, TimeUnit.MINUTES)
        .readTimeout(35, TimeUnit.MINUTES)
        .build()

    @Provides
    @Singleton
    @NormalClient
    fun provideNormalRetrofit(@NormalClient client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(PLACEHOLDER_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    @NoRedirectClient
    fun provideNoRedirectRetrofit(@NoRedirectClient client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(PLACEHOLDER_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    @LongTimeoutClient
    fun provideLongTimeoutRetrofit(@LongTimeoutClient client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(PLACEHOLDER_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideAuthApi(@NormalClient retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideAuthRedirectApi(@NoRedirectClient retrofit: Retrofit): AuthRedirectApi =
        retrofit.create(AuthRedirectApi::class.java)

    @Provides
    @Singleton
    fun provideGenerationApi(@LongTimeoutClient retrofit: Retrofit): GenerationApi =
        retrofit.create(GenerationApi::class.java)

    @Provides
    @Singleton
    fun provideGalleryApi(@NormalClient retrofit: Retrofit): GalleryApi = retrofit.create(GalleryApi::class.java)

    /**
     * Gallery thumbnails/full images are served from authenticated, per-account
     * routes (/files/Images/<user>/...) - Coil's default ImageLoader has no
     * cookies and no idea what server to hit, so every load would fail. This
     * reuses the exact same cookie-jar-equipped, base-URL-rewriting client the
     * rest of the app uses for API calls.
     */
    @Provides
    @Singleton
    fun provideImageLoader(@ApplicationContext context: Context, @NormalClient client: OkHttpClient): ImageLoader =
        ImageLoader.Builder(context)
            .okHttpClient(client)
            .build()

    /** Same reasoning as [provideImageLoader], for ExoPlayer video playback. */
    @Provides
    @Singleton
    fun provideMediaDataSourceFactory(@NormalClient client: OkHttpClient): OkHttpDataSource.Factory =
        OkHttpDataSource.Factory(client)
}
