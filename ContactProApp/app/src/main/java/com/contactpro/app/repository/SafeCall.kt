package com.contactpro.app.repository

import com.contactpro.app.network.ApiResult
import retrofit2.Response

/** Shared helper to safely execute Retrofit calls */
suspend fun <T> safeCall(call: suspend () -> Response<T>): ApiResult<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                ApiResult.Success(body)
            } else {
                ApiResult.Error("Empty response body", response.code())
            }
        } else {
            ApiResult.Error(
                response.errorBody()?.string() ?: "Unknown error",
                response.code()
            )
        }
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Network error")
    }
}
