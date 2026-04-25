package com.contactpro.app.network

/** Generic sealed wrapper for API call results */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int = -1) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}
