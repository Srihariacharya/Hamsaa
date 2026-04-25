package com.contactpro.app.repository

import com.contactpro.app.model.*
import com.contactpro.app.network.ApiResult
import com.contactpro.app.network.ApiService

class InteractionRepository(private val api: ApiService) {

    suspend fun createInteraction(request: InteractionRequest): ApiResult<InteractionResponse> = safeCall {
        api.createInteraction(request)
    }

    suspend fun getInteractions(contactId: Long): ApiResult<List<InteractionResponse>> = safeCall {
        api.getInteractions(contactId)
    }
}
