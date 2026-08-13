package com.shadow.shineyverse.data.remote.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Login/register on this backend signal success/failure purely via a 303's
 * `Location` header (`/ultra...` on success, `/login...`/`/register...` on
 * failure) - no JSON body either way. Must run on the no-redirect-following
 * client so that header is still there to read when the response comes back.
 */
interface AuthRedirectApi {
    @FormUrlEncoded
    @POST("/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("next") next: String = "/ultra",
    ): Response<Unit>

    @FormUrlEncoded
    @POST("/register")
    suspend fun register(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("confirm") confirm: String,
        @Field("next") next: String = "/ultra",
    ): Response<Unit>
}
