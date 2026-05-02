package com.contactpro.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contactpro.app.model.ContactResponse
import com.contactpro.app.network.ApiResult
import com.contactpro.app.network.RetrofitClient
import com.contactpro.app.repository.ContactRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

sealed class FollowUpGroup {
    data class Overdue(val contacts: List<ContactResponse>) : FollowUpGroup()
    data class Today(val contacts: List<ContactResponse>) : FollowUpGroup()
    data class ThisWeek(val contacts: List<ContactResponse>) : FollowUpGroup()
    data class Upcoming(val contacts: List<ContactResponse>) : FollowUpGroup()
    data class NoHistory(val contacts: List<ContactResponse>) : FollowUpGroup()
}

class FollowUpViewModel : ViewModel() {

    private val repo = ContactRepository(RetrofitClient.apiService)

    private val _groups = MutableStateFlow<List<FollowUpGroup>>(emptyList())
    val groups: StateFlow<List<FollowUpGroup>> = _groups.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadFollowUps(userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repo.getContacts(userId)
            if (result is ApiResult.Success) {
                _groups.value = categorizeContacts(result.data)
            }
            _isLoading.value = false
        }
    }

    private fun categorizeContacts(contacts: List<ContactResponse>): List<FollowUpGroup> {
        val overdue = mutableListOf<ContactResponse>()
        val today = mutableListOf<ContactResponse>()
        val thisWeek = mutableListOf<ContactResponse>()
        val upcoming = mutableListOf<ContactResponse>()
        val noHistory = mutableListOf<ContactResponse>()

        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        contacts.forEach { contact ->
            if (contact.followUpFrequency <= 0) return@forEach
            
            val lastDateStr = contact.lastInteractionDate?.take(10)
            if (lastDateStr == null) {
                noHistory.add(contact)
                return@forEach
            }

            val lastDate = sdf.parse(lastDateStr) ?: return@forEach
            
            // Normalize lastDate to midnight to avoid hour-based errors
            val lastCal = Calendar.getInstance()
            lastCal.time = lastDate
            lastCal.set(Calendar.HOUR_OF_DAY, 0)
            lastCal.set(Calendar.MINUTE, 0)
            lastCal.set(Calendar.SECOND, 0)
            lastCal.set(Calendar.MILLISECOND, 0)
            lastCal.add(Calendar.DAY_OF_YEAR, contact.followUpFrequency)
            val nextDate = lastCal.time

            val diffMillis = nextDate.time - now.time
            val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

            when {
                diffDays < -1 -> overdue.add(contact)   // more than 1 day past due
                diffDays <= 1 -> today.add(contact)     // due today or within 1 day (catches rounding)
                diffDays in 2..7 -> thisWeek.add(contact)
                else -> upcoming.add(contact)
            }
        }

        return listOfNotNull(
            if (overdue.isNotEmpty()) FollowUpGroup.Overdue(overdue.sortedBy { it.name }) else null,
            if (today.isNotEmpty()) FollowUpGroup.Today(today.sortedBy { it.name }) else null,
            if (thisWeek.isNotEmpty()) FollowUpGroup.ThisWeek(thisWeek.sortedBy { it.name }) else null,
            if (upcoming.isNotEmpty()) FollowUpGroup.Upcoming(upcoming.sortedBy { it.name }) else null,
            if (noHistory.isNotEmpty()) FollowUpGroup.NoHistory(noHistory.sortedBy { it.name }) else null
        )
    }
}
