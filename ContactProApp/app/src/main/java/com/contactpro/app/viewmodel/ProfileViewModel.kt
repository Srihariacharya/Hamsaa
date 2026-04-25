package com.contactpro.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.contactpro.app.SessionManager
import com.contactpro.app.model.*
import com.contactpro.app.network.ApiResult
import com.contactpro.app.network.RetrofitClient
import com.contactpro.app.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repo    = AuthRepository(RetrofitClient.apiService)
    val session         = SessionManager(application)

    val userName:    Flow<String> = session.userName
    val userEmail:   Flow<String> = session.userEmail
    val userPhone:   Flow<String> = session.userPhone
    val userCompany: Flow<String> = session.userCompany
    val userId:      Flow<Long>   = session.userId

    private val _updateState = MutableStateFlow<ApiResult<UserResponse>?>(null)
    val updateState: StateFlow<ApiResult<UserResponse>?> = _updateState.asStateFlow()

    fun updateProfile(userId: Long, name: String, phone: String?, company: String?) {
        viewModelScope.launch {
            _updateState.value = ApiResult.Loading
            val currentEmail = session.userEmail.first()
            val result = repo.updateProfile(userId, ProfileRequest(
                name = name, 
                email = currentEmail,
                phone = phone, 
                company = company
            ))
            _updateState.value = result
            if (result is ApiResult.Success) {
                session.updateProfile(name, phone, company)
            }
        }
    }

    fun logout() {
        viewModelScope.launch { session.clearSession() }
    }

    fun setTheme(mode: String) {
        viewModelScope.launch { session.setTheme(mode) }
    }

    fun changePassword(userId: Long, currentPass: String, newPass: String) {
        viewModelScope.launch {
            _updateState.value = ApiResult.Loading
            val currentEmail = session.userEmail.first()
            val currentName = session.userName.first()
            
            // Send current name/email along with password to be 100% safe
            val result = repo.updateProfile(userId, ProfileRequest(
                name = currentName,
                email = currentEmail,
                currentPassword = currentPass,
                newPassword = newPass
            ))
            _updateState.value = result
        }
    }

    fun resetUpdateState() { _updateState.value = null }
}
