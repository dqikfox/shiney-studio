package com.shadow.shineyverse.data.remote.api

import com.shadow.shineyverse.data.remote.dto.GenerateResponseDto
import com.shadow.shineyverse.data.remote.dto.ModelInfoDto
import com.shadow.shineyverse.data.remote.dto.UploadImageResponseDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Bound to the long-timeout client - `/generate` alone can legitimately block
 * for many minutes server-side, and a long ceiling doesn't slow down the
 * other two calls here, it just avoids aborting a slow one early.
 */
interface GenerationApi {
    @GET("/models")
    suspend fun getModels(): Response<Map<String, ModelInfoDto>>

    @Multipart
    @POST("/upload_image")
    suspend fun uploadImage(@Part file: MultipartBody.Part): Response<UploadImageResponseDto>

    @FormUrlEncoded
    @POST("/generate")
    suspend fun generate(
        @Field("model") model: String,
        @Field("prompt") prompt: String,
        @Field("negative") negative: String,
        @Field("width") width: Int,
        @Field("height") height: Int,
        @Field("steps") steps: Int,
        @Field("cfg") cfg: Float,
        @Field("seed") seed: Long,
        @Field("sampler") sampler: String,
        @Field("scheduler") scheduler: String,
        @Field("length") length: Int,
        @Field("fps") fps: Int,
        @Field("image_filename") imageFilename: String,
        @Field("folder") folder: String,
    ): Response<GenerateResponseDto>
}
