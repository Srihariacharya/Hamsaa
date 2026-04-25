package com.contactpro.app.repository

import com.contactpro.app.model.*
import com.contactpro.app.network.ApiResult
import com.contactpro.app.network.ApiService
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ContactRepository(private val api: ApiService) {

    suspend fun getContacts(userId: Long): ApiResult<List<ContactResponse>> = safeCall {
        api.getContacts(userId)
    }

    suspend fun createContact(request: ContactRequest): ApiResult<ContactResponse> = safeCall {
        api.createContact(request)
    }

    suspend fun updateContact(contactId: Long, userId: Long, request: ContactRequest): ApiResult<ContactResponse> = safeCall {
        api.updateContact(contactId, userId, request)
    }

    suspend fun deleteContact(contactId: Long, userId: Long): ApiResult<String> = safeCall {
        api.deleteContact(contactId, userId)
    }

    suspend fun getContactById(contactId: Long): ApiResult<ContactResponse> = safeCall {
        api.getContactById(contactId)
    }

    suspend fun toggleFavorite(contactId: Long, userId: Long): ApiResult<ContactResponse> = safeCall {
        api.toggleFavorite(contactId, userId)
    }

    suspend fun toggleBlock(contactId: Long, userId: Long): ApiResult<ContactResponse> = safeCall {
        api.toggleBlock(contactId, userId)
    }

    suspend fun getFavorites(userId: Long): ApiResult<List<ContactResponse>> = safeCall {
        api.getFavorites(userId)
    }

    suspend fun getBlocked(userId: Long): ApiResult<List<ContactResponse>> = safeCall {
        api.getBlocked(userId)
    }

    suspend fun importVcf(userId: Long, file: File): ApiResult<String> = safeCall {
        val requestBody = file.asRequestBody()
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
        api.importVcf(userId, part)
    }

    /** Simple name-based search — returns all matching contacts across all users */
    suspend fun searchByName(name: String): ApiResult<List<ContactResponse>> = safeCall {
        api.searchContacts(name)
    }

    /** Paginated search scoped to a specific user with keyword */
    suspend fun searchPaged(
        userId: Long,
        keyword: String,
        page: Int = 0,
        size: Int = 20
    ): ApiResult<PagedResponse<ContactResponse>> = safeCall {
        api.searchContactsPaged(userId, keyword, page, size)
    }
}
