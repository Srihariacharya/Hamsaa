package com.contactpro.app.repository

import com.contactpro.app.model.AnalyticsResponse
import com.contactpro.app.network.ApiResult
import com.contactpro.app.network.ApiService

class AnalyticsRepository(private val api: ApiService) {

    suspend fun getAnalytics(userId: Long): ApiResult<AnalyticsResponse> = safeCall {
        api.getAnalytics(userId)
    }
}
