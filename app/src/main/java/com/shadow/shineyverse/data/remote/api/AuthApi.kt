package com.shadow.shineyverse.data.remote.api

import com.shadow.shineyverse.data.remote.dto.StatsDto
import retrofit2.Response
import retrofit2.http.GET

/** Calls that don't depend on inspecting a redirect - normal, redirect-following client. */
interface AuthApi {
    /** Used purely as a cheap "is my session still valid" probe at cold start. */
    @GET("/stats")
    suspend fun checkSession(): Response<StatsDto>

    @GET("/logout")
    suspend fun logout(): Response<Unit>
}
