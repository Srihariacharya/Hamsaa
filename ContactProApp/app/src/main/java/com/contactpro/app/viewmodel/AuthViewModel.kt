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
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AuthRepository(RetrofitClient.apiService)
    val session = SessionManager(application)

    private val _loginState = MutableStateFlow<ApiResult<LoginResponse>?>(null)
    val loginState: StateFlow<ApiResult<LoginResponse>?> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<ApiResult<UserResponse>?>(null)
    val registerState: StateFlow<ApiResult<UserResponse>?> = _registerState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = ApiResult.Loading
            val result = repo.login(email, password)
            _loginState.value = result
            if (result is ApiResult.Success) {
                val d = result.data
                session.saveSession(d.userId, d.name, d.email, d.phone, d.company)
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = ApiResult.Loading
            _registerState.value = repo.register(name, email, password)
        }
    }

    fun resetLoginState()    { _loginState.value    = null }
    fun resetRegisterState() { _registerState.value = null }
}
