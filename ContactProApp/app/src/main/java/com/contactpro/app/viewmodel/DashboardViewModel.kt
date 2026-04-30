package com.contactpro.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.contactpro.app.model.*
import com.contactpro.app.network.ApiResult
import com.contactpro.app.network.RetrofitClient
import com.contactpro.app.repository.AnalyticsRepository
import com.contactpro.app.repository.ContactRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardUiState(
    val userName: String = "Agent",
    val totalContacts: Int = 0,
    val totalInteractions: Int = 0,
    val activeContacts: Int = 0,
    val avgDuration: Double = 0.0,
    val taskCompletionRate: Double = 0.0,
    val interactionTrends: List<TrendPoint> = emptyList(),
    val taskDistribution: List<DistributionPoint> = emptyList(),
    val mostContacted: List<ContactResponse> = emptyList(),
    val inactiveContacts: List<ContactResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val analyticsRepo = AnalyticsRepository(RetrofitClient.apiService)
    private val contactRepo   = ContactRepository(RetrofitClient.apiService)
    private val session       = com.contactpro.app.SessionManager(application)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun loadDashboard(userId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val analyticsDeferred = async { analyticsRepo.getAnalytics(userId) }
            val contactsDeferred  = async { contactRepo.getContacts(userId)    }
            val nameFlow = session.userName

            val analyticsResult = analyticsDeferred.await()
            val contactsResult  = contactsDeferred.await()
            val name = nameFlow.first()

            val analytics = (analyticsResult as? ApiResult.Success)?.data
            val contacts  = (contactsResult  as? ApiResult.Success)?.data ?: emptyList()

            // Sort contacts by last interaction date to determine most/inactive
            val sorted = contacts.sortedByDescending { it.lastInteractionDate }
            val mostContacted = sorted.take(5)
            val inactive = contacts
                .filter { it.lastInteractionDate == null || it.followUpFrequency > 0 }
                .take(10)

            // Ensure labels are Week 1, Week 2, etc.
            val rawTrends = analytics?.interactionTrends ?: emptyList()
            val trends = rawTrends.mapIndexed { index, point ->
                point.copy(name = "Week ${index + 1}")
            }

            _uiState.value = DashboardUiState(
                userName          = name.ifBlank { "Executive Agent" },
                totalContacts     = contacts.size,
                totalInteractions = analytics?.totalInteractions ?: 0,
                activeContacts    = analytics?.activeContacts    ?: 0,
                avgDuration       = analytics?.avgDuration       ?: 0.0,
                taskCompletionRate = analytics?.taskCompletionRate ?: 0.0,
                interactionTrends = trends,
                taskDistribution  = analytics?.taskDistribution  ?: emptyList(),
                mostContacted     = mostContacted,
                inactiveContacts  = inactive,
                isLoading         = false,
                error             = if (analyticsResult is ApiResult.Error) analyticsResult.message else null
            )
        }
    }
}
