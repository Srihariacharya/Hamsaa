package com.contactpro.app.model

// ─── Auth ────────────────────────────────────────────────────────────────────

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val userId: Long,
    val email: String,
    val name: String,
    val phone: String?,
    val company: String?
)

data class UserRequest(
    val name: String,
    val email: String,
    val password: String
)

data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String?,
    val company: String?
)

data class ProfileRequest(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val company: String? = null,
    val currentPassword: String? = null,
    val newPassword: String? = null
)
