package com.shadow.shineyverse.data.remote

/**
 * Transport-layer result, distinct from an endpoint's own body-level shape
 * (e.g. mutation endpoints' `{"ok": bool, "error"?: str}`). The backend has
 * no single universal response envelope, so this only models transport
 * outcomes; body-level success/failure is branched by callers on the
 * decoded DTO itself.
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data object Unauthorized : ApiResult<Nothing>()
    data object Forbidden : ApiResult<Nothing>()
    data class ValidationError(val rawBody: String?) : ApiResult<Nothing>()
    data class NetworkError(val throwable: Throwable) : ApiResult<Nothing>()
    data class UnknownHttpError(val code: Int, val rawBody: String?) : ApiResult<Nothing>()
}

inline fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) action(data)
    return this
}

/** Transforms a successful payload while passing every failure case through unchanged. */
inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(data))
    is ApiResult.Unauthorized -> this
    is ApiResult.Forbidden -> this
    is ApiResult.ValidationError -> this
    is ApiResult.NetworkError -> this
    is ApiResult.UnknownHttpError -> this
}

/** Human-readable message for the common failure cases, suitable for direct display. */
fun ApiResult<*>.errorMessage(): String = when (this) {
    is ApiResult.Success -> ""
    is ApiResult.Unauthorized -> "Your session expired - please log in again."
    is ApiResult.Forbidden -> "You don't have permission to do that."
    is ApiResult.ValidationError -> "That request wasn't valid."
    is ApiResult.NetworkError -> "Couldn't reach the server - check the address in Settings and that it's running."
    is ApiResult.UnknownHttpError -> "Something went wrong (HTTP $code)."
}
