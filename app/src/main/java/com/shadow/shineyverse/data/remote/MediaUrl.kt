package com.shadow.shineyverse.data.remote

/** Never routed anywhere real - every request's host is rewritten by [DynamicBaseUrlInterceptor]. */
const val PLACEHOLDER_BASE_URL = "http://ultra-studio.invalid/"

/**
 * `/gallery` (and other endpoints) return media paths like "/files/Images/user/x.png",
 * relative to whatever server is currently configured - the same way a browser resolves
 * a relative URL against the page's own origin. Coil and Media3 have no such origin
 * context, so this makes the path syntactically absolute (using the same placeholder
 * host Retrofit uses) so [DynamicBaseUrlInterceptor] can rewrite it to the real server
 * at request time.
 */
fun String.toAbsoluteMediaUrl(): String =
    if (startsWith("http://") || startsWith("https://")) this else PLACEHOLDER_BASE_URL.trimEnd('/') + this
