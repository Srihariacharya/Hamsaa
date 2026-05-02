package com.contactpro.app.network

import com.contactpro.app.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: UserRequest): Response<UserResponse>

    @GET("api/auth/profile/{userId}")
    suspend fun getProfile(@Path("userId") userId: Long): Response<UserResponse>

    @PUT("api/auth/profile/{userId}")
    suspend fun updateProfile(
        @Path("userId") userId: Long,
        @Body request: ProfileRequest
    ): Response<UserResponse>

    // ── Contacts ──────────────────────────────────────────────────────────────

    @GET("api/contacts/user/{userId}")
    suspend fun getContacts(@Path("userId") userId: Long): Response<List<ContactResponse>>

    @POST("api/contacts")
    suspend fun createContact(@Body request: ContactRequest): Response<ContactResponse>

    @POST("api/contacts/batch")
    suspend fun createContactsBatch(@Body requests: List<ContactRequest>): Response<List<ContactResponse>>

    @PUT("api/contacts/{contactId}")
    suspend fun updateContact(
        @Path("contactId") contactId: Long,
        @Query("userId") userId: Long,
        @Body request: ContactRequest
    ): Response<ContactResponse>

    @DELETE("api/contacts/{contactId}")
    suspend fun deleteContact(
        @Path("contactId") contactId: Long,
        @Query("userId") userId: Long
    ): Response<String>

    @GET("api/contacts/{contactId}")
    suspend fun getContactById(@Path("contactId") contactId: Long): Response<ContactResponse>

    @PATCH("api/contacts/{contactId}/favorite")
    suspend fun toggleFavorite(
        @Path("contactId") contactId: Long,
        @Query("userId") userId: Long
    ): Response<ContactResponse>

    @PATCH("api/contacts/{contactId}/block")
    suspend fun toggleBlock(
        @Path("contactId") contactId: Long,
        @Query("userId") userId: Long
    ): Response<ContactResponse>

    @GET("api/contacts/favorites")
    suspend fun getFavorites(@Query("userId") userId: Long): Response<List<ContactResponse>>

    @GET("api/contacts/blocked")
    suspend fun getBlocked(@Query("userId") userId: Long): Response<List<ContactResponse>>

    // Simple search by name (no userId required)
    @GET("api/contacts/search")
    suspend fun searchContacts(
        @Query("name") name: String
    ): Response<List<ContactResponse>>

    // Paginated search scoped to a user
    @GET("api/contacts/search/{userId}")
    suspend fun searchContactsPaged(
        @Path("userId")  userId:  Long,
        @Query("keyword") keyword: String,
        @Query("page")   page:    Int = 0,
        @Query("size")   size:    Int = 20
    ): Response<PagedResponse<ContactResponse>>

    @Multipart
    @POST("api/contacts/import-vcf")
    suspend fun importVcf(
        @Query("userId") userId: Long,
        @Part file: MultipartBody.Part
    ): Response<String>

    // ── Interactions ──────────────────────────────────────────────────────────

    @POST("api/interactions")
    suspend fun createInteraction(@Body request: InteractionRequest): Response<InteractionResponse>

    @POST("api/interactions/batch")
    suspend fun createInteractionsBatch(@Body requests: List<InteractionRequest>): Response<List<InteractionResponse>>

    @GET("api/interactions/contact/{contactId}")
    suspend fun getInteractions(@Path("contactId") contactId: Long): Response<List<InteractionResponse>>

    @GET("api/interactions/user/{userId}")
    suspend fun getInteractionsByUser(@Path("userId") userId: Long): Response<List<InteractionResponse>>

    @DELETE("api/interactions/cleanup/{userId}")
    suspend fun cleanupCorruptedInteractions(@Path("userId") userId: Long): Response<String>

    @DELETE("api/interactions/reset/{userId}")
    suspend fun resetInteractions(@Path("userId") userId: Long): Response<String>

    @DELETE("api/contacts/deduplicate/{userId}")
    suspend fun deduplicateContacts(@Path("userId") userId: Long): Response<String>

    // ── Analytics ─────────────────────────────────────────────────────────────

    @GET("api/analytics/user/{userId}")
    suspend fun getAnalytics(@Path("userId") userId: Long): Response<AnalyticsResponse>

    // ── Tasks ─────────────────────────────────────────────────────────────────

    @GET("api/tasks/user/{userId}")
    suspend fun getTasks(@Path("userId") userId: Long): Response<List<TaskResponse>>

    @POST("api/tasks")
    suspend fun createTask(@Body request: TaskRequest): Response<TaskResponse>

    @PUT("api/tasks/{taskId}")
    suspend fun updateTask(
        @Path("taskId") taskId: Long,
        @Body request: TaskRequest
    ): Response<TaskResponse>

    @DELETE("api/tasks/{taskId}")
    suspend fun deleteTask(@Path("taskId") taskId: Long): Response<Void>
}
