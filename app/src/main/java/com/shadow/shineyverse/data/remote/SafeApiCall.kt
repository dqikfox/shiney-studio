package com.shadow.shineyverse.data.remote

import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Wraps a Retrofit suspend call, turning transport/HTTP outcomes into [ApiResult]
 * so every repository handles the same small set of cases instead of raw exceptions.
 */
suspend fun <T> safeApiCall(block: suspend () -> Response<T>): ApiResult<T> {
    return try {
        val response = block()
        val body = response.body()
        when {
            response.isSuccessful && body != null -> ApiResult.Success(body)
            response.isSuccessful && body == null -> {
                @Suppress("UNCHECKED_CAST")
                ApiResult.Success(Unit as T)
            }
            response.code() == 401 -> ApiResult.Unauthorized
            response.code() == 403 -> ApiResult.Forbidden
            response.code() == 422 -> ApiResult.ValidationError(response.errorBody()?.string())
            else -> ApiResult.UnknownHttpError(response.code(), response.errorBody()?.string())
        }
    } catch (e: HttpException) {
        when (e.code()) {
            401 -> ApiResult.Unauthorized
            403 -> ApiResult.Forbidden
            422 -> ApiResult.ValidationError(e.response()?.errorBody()?.string())
            else -> ApiResult.UnknownHttpError(e.code(), e.response()?.errorBody()?.string())
        }
    } catch (e: IOException) {
        ApiResult.NetworkError(e)
    }
}
