package com.shadow.shineyverse.data.remote.api

import com.shadow.shineyverse.data.remote.dto.FolderDto
import com.shadow.shineyverse.data.remote.dto.GalleryResponseDto
import com.shadow.shineyverse.data.remote.dto.MutationResultDto
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface GalleryApi {
    @GET("/gallery")
    suspend fun getGallery(
        @Query("folder") folder: String = "",
        @Query("limit") limit: Int = 60,
        @Query("offset") offset: Int = 0,
    ): Response<GalleryResponseDto>

    @GET("/folders")
    suspend fun getFolders(): Response<List<FolderDto>>

    @FormUrlEncoded
    @POST("/folders/create")
    suspend fun createFolder(@Field("name") name: String): Response<MutationResultDto>

    @FormUrlEncoded
    @POST("/folders/rename")
    suspend fun renameFolder(
        @Field("old_name") oldName: String,
        @Field("new_name") newName: String,
    ): Response<MutationResultDto>

    @FormUrlEncoded
    @POST("/folders/delete")
    suspend fun deleteFolder(@Field("name") name: String): Response<MutationResultDto>

    @FormUrlEncoded
    @POST("/move_item")
    suspend fun moveItem(
        @Field("name") name: String,
        @Field("kind") kind: String,
        @Field("folder") folder: String = "",
    ): Response<MutationResultDto>

    @FormUrlEncoded
    @POST("/copy_item")
    suspend fun copyItem(
        @Field("name") name: String,
        @Field("kind") kind: String,
        @Field("folder") folder: String = "",
    ): Response<MutationResultDto>

    @FormUrlEncoded
    @POST("/delete_item")
    suspend fun deleteItem(
        @Field("name") name: String,
        @Field("kind") kind: String,
    ): Response<MutationResultDto>
}
