package com.shadow.shineyverse.di

import javax.inject.Qualifier

/** The app-wide OkHttpClient/Retrofit: normal redirect-following, normal timeouts. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NormalClient

/**
 * A client that never follows redirects, for the two endpoints (`/login`,
 * `/register`) whose success/failure is signaled purely by a 303's
 * `Location` header - OkHttp's default auto-follow would swallow that
 * signal before Retrofit ever sees it.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NoRedirectClient

/** Long call/read timeouts for `/generate`, which can block for many minutes server-side. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LongTimeoutClient
