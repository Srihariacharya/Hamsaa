package com.contactpro.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.contactpro.app.model.*
import com.contactpro.app.network.ApiResult
import com.contactpro.app.network.RetrofitClient
import com.contactpro.app.repository.InteractionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InteractionViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = InteractionRepository(RetrofitClient.apiService)

    private val _interactions = MutableStateFlow<ApiResult<List<InteractionResponse>>>(ApiResult.Loading)
    val interactions: StateFlow<ApiResult<List<InteractionResponse>>> = _interactions.asStateFlow()

    private val _createState = MutableStateFlow<ApiResult<InteractionResponse>?>(null)
    val createState: StateFlow<ApiResult<InteractionResponse>?> = _createState.asStateFlow()

    fun loadInteractions(contactId: Long) {
        viewModelScope.launch {
            _interactions.value = ApiResult.Loading
            _interactions.value = repo.getInteractions(contactId)
        }
    }

    fun createInteraction(contactId: Long, type: String, notes: String?, duration: Int?) {
        viewModelScope.launch {
            _createState.value = ApiResult.Loading
            _createState.value = repo.createInteraction(
                InteractionRequest(
                    type = type,
                    notes = notes,
                    duration = duration?.toLong(),
                    contactId = contactId
                )
            )
        }
    }

    fun resetCreateState() { _createState.value = null }
}
