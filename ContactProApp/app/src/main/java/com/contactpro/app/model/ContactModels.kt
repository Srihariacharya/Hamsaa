package com.contactpro.app.model

import com.google.gson.annotations.SerializedName

// ─── Contact ──────────────────────────────────────────────────────────────────

data class ContactRequest(
    val name: String,
    val phone: String,
    val email: String?,
    val category: String?,
    val notes: String?,
    val gender: String?,
    val dob: String?,
    val followUpFrequency: Int = 0,
    val userId: Long
)

data class ContactResponse(
    val id: Long,
    val name: String,
    val phone: String,
    val email: String?,
    val category: String?,
    val gender: String?,
    val dob: String?,
    val notes: String?,
    val followUpFrequency: Int,
    val lastInteractionDate: String?,
    @SerializedName("blocked")
    val isBlocked: Boolean,
    @SerializedName("favorite")
    val isFavorite: Boolean,
    val createdAt: String?
)

/** Represents a contact fetched from the device */
data class DeviceContact(
    val id: String,
    val name: String,
    val phone: String,
    val gender: String? = null,
    var selected: Boolean = false
)

/** Wraps Spring's Page<T> JSON response */
data class PagedResponse<T>(
    val content:       List<T>,
    val totalElements: Long,
    val totalPages:    Int,
    val number:        Int   // current page index
)

// ─── Interaction ─────────────────────────────────────────────────────────────

data class InteractionRequest(
    val type: String,        // CALL, EMAIL, MEETING, NOTE
    val notes: String?,
    val duration: Long?,     // in seconds
    val contactId: Long,
    val interactionDate: String? = null // ISO string, defaults to now on server
)

data class InteractionResponse(
    val id: Long,
    val type: String,
    val notes: String?,
    val duration: Long?,
    val interactionDate: String,
    val contactId: Long
)

// ─── Task ──────────────────────────────────────────────────────────────────

data class TaskRequest(
    val title: String,
    val description: String?,
    val dueDate: String?,
    val priority: String, // LOW, MEDIUM, HIGH
    val status: String,   // PENDING, IN_PROGRESS, COMPLETED
    val contactId: Long?,
    val userId: Long
)

data class TaskResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val dueDate: String?,
    val priority: String,
    val status: String,
    val contactId: Long?,
    val userId: Long,
    val createdAt: String
)

// ─── Analytics ─────────────────────────────────────────────────────────────

data class AnalyticsResponse(
    val totalContacts: Int,
    val activeContacts: Int,
    val totalInteractions: Int,
    val avgDuration: Double,
    val taskCompletionRate: Double,
    val categoryDistribution: Map<String, Int>?,
    val interactionTrends: List<TrendPoint>?,
    val taskDistribution: List<DistributionPoint>?
)

data class TrendPoint(val name: String, val value: Int)
data class DistributionPoint(val name: String, val value: Int)

// ─── Device Logs ───────────────────────────────────────────────────────────

data class CallLogEntry(
    val number: String,
    val name: String?,
    val duration: Long,
    val type: Int,
    val date: Long
)
