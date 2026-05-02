package com.contactpro.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.contactpro.app.SessionManager
import com.contactpro.app.model.*
import com.contactpro.app.network.ApiResult
import com.contactpro.app.network.RetrofitClient
import com.contactpro.app.repository.ContactRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.*
import java.io.File

class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ContactRepository(RetrofitClient.apiService)
    private val session = SessionManager(application)

    // Expose userId from session
    val userId: Flow<Long> = session.userId

    private val _contacts = MutableStateFlow<ApiResult<List<ContactResponse>>>(ApiResult.Loading)
    val contacts: StateFlow<ApiResult<List<ContactResponse>>> = _contacts.asStateFlow()

    private val _contactDetail = MutableStateFlow<ApiResult<ContactResponse>?>(null)
    val contactDetail: StateFlow<ApiResult<ContactResponse>?> = _contactDetail.asStateFlow()

    private val _createState = MutableStateFlow<ApiResult<ContactResponse>?>(null)
    val createState: StateFlow<ApiResult<ContactResponse>?> = _createState.asStateFlow()

    private val _deleteState = MutableStateFlow<ApiResult<String>?>(null)
    val deleteState: StateFlow<ApiResult<String>?> = _deleteState.asStateFlow()

    private val _importState = MutableStateFlow<ApiResult<String>?>(null)
    val importState: StateFlow<ApiResult<String>?> = _importState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredContacts: Flow<List<ContactResponse>> = combine(_contacts, _searchQuery) { result, query ->
        if (result is ApiResult.Success) {
            if (query.isBlank()) result.data
            else result.data.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.phone.contains(query, ignoreCase = true) ||
                it.email?.contains(query, ignoreCase = true) == true
            }
        } else emptyList()
    }

    fun loadContacts(userId: Long) {
        viewModelScope.launch {
            _contacts.value = ApiResult.Loading
            _contacts.value = repo.getContacts(userId)
        }
    }

    fun loadContactDetail(contactId: Long) {
        viewModelScope.launch {
            _contactDetail.value = ApiResult.Loading
            _contactDetail.value = repo.getContactById(contactId)
        }
    }

    fun createContact(request: ContactRequest) {
        viewModelScope.launch {
            _createState.value = ApiResult.Loading
            _createState.value = repo.createContact(request)
        }
    }

    fun updateContact(contactId: Long, userId: Long, request: ContactRequest) {
        viewModelScope.launch {
            _createState.value = ApiResult.Loading
            _createState.value = repo.updateContact(contactId, userId, request)
        }
    }

    fun deleteContact(contactId: Long, userId: Long) {
        viewModelScope.launch {
            _deleteState.value = ApiResult.Loading
            _deleteState.value = repo.deleteContact(contactId, userId)
        }
    }

    fun deleteContactsBatch(contactIds: List<Long>, userId: Long) {
        viewModelScope.launch {
            _deleteState.value = ApiResult.Loading
            val result = repo.deleteContactsBatch(contactIds, userId)
            _deleteState.value = result
            if (result is ApiResult.Success) {
                loadContacts(userId)
            }
        }
    }

    fun toggleFavorite(contactId: Long, userId: Long) {
        viewModelScope.launch {
            // Optimistic update for detail
            val currentDetailResult = _contactDetail.value
            if (currentDetailResult is ApiResult.Success && currentDetailResult.data.id == contactId) {
                _contactDetail.value = ApiResult.Success(currentDetailResult.data.copy(isFavorite = !currentDetailResult.data.isFavorite))
            }
            
            // Optimistic update for list
            val currentListResult = _contacts.value
            if (currentListResult is ApiResult.Success) {
                val updatedList = currentListResult.data.map {
                    if (it.id == contactId) it.copy(isFavorite = !it.isFavorite) else it
                }
                _contacts.value = ApiResult.Success(updatedList)
            }
            
            val result = repo.toggleFavorite(contactId, userId)
            if (result is ApiResult.Error) {
                // Rollback if error
                loadContacts(userId)
                loadContactDetail(contactId)
            }
        }
    }

    fun toggleBlock(contactId: Long, userId: Long) {
        viewModelScope.launch {
            // Optimistic update for detail
            val currentDetailResult = _contactDetail.value
            if (currentDetailResult is ApiResult.Success && currentDetailResult.data.id == contactId) {
                _contactDetail.value = ApiResult.Success(currentDetailResult.data.copy(isBlocked = !currentDetailResult.data.isBlocked))
            }
            
            // Optimistic update for list
            val currentListResult = _contacts.value
            if (currentListResult is ApiResult.Success) {
                val updatedList = currentListResult.data.map {
                    if (it.id == contactId) it.copy(isBlocked = !it.isBlocked) else it
                }
                _contacts.value = ApiResult.Success(updatedList)
            }

            val result = repo.toggleBlock(contactId, userId)
            if (result is ApiResult.Error) {
                // Rollback if error
                loadContactDetail(contactId)
                loadContacts(userId)
            }
        }
    }

    fun importVcf(userId: Long, file: File) {
        viewModelScope.launch {
            _importState.value = ApiResult.Loading
            _importState.value = repo.importVcf(userId, file)
        }
    }

    fun setSearchQuery(q: String) { _searchQuery.value = q }
    fun resetCreateState()  { _createState.value  = null }
    fun resetDeleteState()  { _deleteState.value  = null }
    fun resetImportState()  { _importState.value  = null }

    // ── Remote search (uses backend ?name= endpoint) ──────────────────────────
    private val _searchResults = MutableStateFlow<ApiResult<List<ContactResponse>>?>(null)
    val searchResults: StateFlow<ApiResult<List<ContactResponse>>?> = _searchResults.asStateFlow()

    fun searchRemote(name: String) {
        if (name.isBlank()) { _searchResults.value = null; return }
        viewModelScope.launch {
            _searchResults.value = ApiResult.Loading
            _searchResults.value = repo.searchByName(name)
        }
    }

    fun clearSearchResults() { _searchResults.value = null }
}
