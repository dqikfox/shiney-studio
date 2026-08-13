package com.shadow.shineyverse.data.repository

import com.shadow.shineyverse.data.remote.ApiResult
import com.shadow.shineyverse.data.remote.api.GalleryApi
import com.shadow.shineyverse.data.remote.dto.FolderDto
import com.shadow.shineyverse.data.remote.dto.GalleryResponseDto
import com.shadow.shineyverse.data.remote.dto.MutationResultDto
import com.shadow.shineyverse.data.remote.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GalleryRepository @Inject constructor(
    private val galleryApi: GalleryApi,
) {
    suspend fun fetchGallery(folder: String, limit: Int, offset: Int): ApiResult<GalleryResponseDto> =
        safeApiCall { galleryApi.getGallery(folder, limit, offset) }

    suspend fun fetchFolders(): ApiResult<List<FolderDto>> = safeApiCall { galleryApi.getFolders() }

    suspend fun createFolder(name: String): ApiResult<MutationResultDto> = safeApiCall { galleryApi.createFolder(name) }

    suspend fun renameFolder(oldName: String, newName: String): ApiResult<MutationResultDto> =
        safeApiCall { galleryApi.renameFolder(oldName, newName) }

    suspend fun deleteFolder(name: String): ApiResult<MutationResultDto> = safeApiCall { galleryApi.deleteFolder(name) }

    suspend fun moveItem(name: String, kind: String, folder: String): ApiResult<MutationResultDto> =
        safeApiCall { galleryApi.moveItem(name, kind, folder) }

    suspend fun copyItem(name: String, kind: String, folder: String): ApiResult<MutationResultDto> =
        safeApiCall { galleryApi.copyItem(name, kind, folder) }

    suspend fun deleteItem(name: String, kind: String): ApiResult<MutationResultDto> =
        safeApiCall { galleryApi.deleteItem(name, kind) }
}
