package com.contactpro.app.repository

import com.contactpro.app.model.*
import com.contactpro.app.network.ApiResult
import com.contactpro.app.network.ApiService

class AuthRepository(private val api: ApiService) {

    suspend fun login(email: String, password: String): ApiResult<LoginResponse> = safeCall {
        api.login(LoginRequest(email, password))
    }

    suspend fun register(name: String, email: String, password: String): ApiResult<UserResponse> = safeCall {
        api.register(UserRequest(name, email, password))
    }

    suspend fun getProfile(userId: Long): ApiResult<UserResponse> = safeCall {
        api.getProfile(userId)
    }

    suspend fun updateProfile(userId: Long, request: ProfileRequest): ApiResult<UserResponse> = safeCall {
        api.updateProfile(userId, request)
    }
}
