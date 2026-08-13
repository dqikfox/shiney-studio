package com.shadow.shineyverse.data.repository

import com.shadow.shineyverse.data.remote.ApiResult
import com.shadow.shineyverse.data.remote.api.GenerationApi
import com.shadow.shineyverse.data.remote.dto.ModelInfoDto
import com.shadow.shineyverse.data.remote.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelsRepository @Inject constructor(
    private val generationApi: GenerationApi,
) {
    suspend fun fetchModels(): ApiResult<Map<String, ModelInfoDto>> = safeApiCall { generationApi.getModels() }
}
