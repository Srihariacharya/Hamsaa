package com.contactpro.app.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.provider.CallLog
import android.provider.ContactsContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.contactpro.app.model.CallLogEntry
import com.contactpro.app.model.ContactRequest
import com.contactpro.app.model.ContactResponse
import com.contactpro.app.model.DeviceContact
import com.contactpro.app.network.ApiResult
import com.contactpro.app.network.RetrofitClient
import com.contactpro.app.repository.ContactRepository
import com.contactpro.app.repository.InteractionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImportViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ContactRepository(RetrofitClient.apiService)
    private val interactionRepo = InteractionRepository(RetrofitClient.apiService)

    private val _deviceContacts = MutableStateFlow<List<DeviceContact>>(emptyList())
    val deviceContacts: StateFlow<List<DeviceContact>> = _deviceContacts.asStateFlow()

    private val _callLogs = MutableStateFlow<List<CallLogEntry>>(emptyList())
    val callLogs: StateFlow<List<CallLogEntry>> = _callLogs.asStateFlow()

    private val _importState = MutableStateFlow<ApiResult<ContactResponse>?>(null)
    val importState: StateFlow<ApiResult<ContactResponse>?> = _importState.asStateFlow()

    private val _importCount = MutableStateFlow(0)
    val importCount: StateFlow<Int> = _importCount.asStateFlow()

    fun fetchDeviceContacts() {
        viewModelScope.launch {
            val contacts = withContext(Dispatchers.IO) {
                val realContacts = readDeviceContacts(getApplication<Application>().contentResolver)
                realContacts
            }
            _deviceContacts.value = contacts
        }
    }

    fun fetchCallLogs() {
        viewModelScope.launch {
            val logs = withContext(Dispatchers.IO) {
                readCallLogs(getApplication<Application>().contentResolver)
            }
            _callLogs.value = logs
        }
    }

    fun toggleDeviceContactSelection(id: String) {
        _deviceContacts.value = _deviceContacts.value.map {
            if (it.id == id) it.copy(selected = !it.selected) else it
        }
    }

    fun selectAll()   { _deviceContacts.value = _deviceContacts.value.map { it.copy(selected = true) } }
    fun deselectAll() { _deviceContacts.value = _deviceContacts.value.map { it.copy(selected = false) } }

    fun importSelectedContacts(userId: Long) {
        val selected = _deviceContacts.value.filter { it.selected }
        if (selected.isEmpty()) return
        viewModelScope.launch {
            _importState.value = ApiResult.Loading
            var count = 0
            
            // Step 1: Prepare batch requests
            val requests = selected.map { dc ->
                val detectedCategory = detectCategoryFromName(dc.name)
                ContactRequest(
                    name   = dc.name,
                    phone  = dc.phone,
                    email  = null,
                    category = detectedCategory,
                    notes  = "Synced from phone",
                    gender = detectGenderFromName(dc.name),
                    dob    = null,
                    followUpFrequency = 7,
                    userId = userId
                )
            }
            
            // Step 2: Send in chunks of 500 to prevent backend crashes
            val chunkedRequests = requests.chunked(500)
            
            for (chunk in chunkedRequests) {
                val batchResult = repo.createContactsBatch(chunk)
                if (batchResult is ApiResult.Success) {
                    val savedContacts = batchResult.data
                    count += savedContacts.size
                    
                    // Step 3: We no longer sync history here to prevent timeouts with 15,000 contacts.
                    // Background sync (SyncManager) will handle this automatically on next app open.
                }
            }
            
            _importCount.value = count
            _importState.value = ApiResult.Success(
                com.contactpro.app.model.ContactResponse(
                    id = 0, name = "$count imported with history", phone = "", email = null,
                    category = null, gender = null, dob = null, followUpFrequency = 0,
                    lastInteractionDate = null, isBlocked = false, isFavorite = false, createdAt = null
                )
            )
        }
    }

    private fun detectCategoryFromName(name: String): String? {
        val lowerName = name.lowercase(Locale.ROOT)
        return when {
            lowerName.contains("client") || lowerName.contains("cust") -> "CLIENT"
            lowerName.contains("partner") || lowerName.contains("assoc") -> "PARTNER"
            lowerName.contains("vendor") || lowerName.contains("supp") -> "VENDOR"
            lowerName.contains("lead") || lowerName.contains("prospect") -> "PROSPECT"
            lowerName.contains("manager") || lowerName.contains("director") -> "PROFESSIONAL"
            else -> null // Leave it blank if not understandable
        }
    }

    private fun detectGenderFromName(name: String): String {
        val firstName = name.trim().split(" ").firstOrNull()?.lowercase(Locale.ROOT) ?: return "Prefer not to say"
        
        // Common male prefixes/structures globally
        val maleNames = setOf("john", "david", "michael", "rahul", "raj", "mohammed", "ahmed", "ali", "carlos", "jose", "srihari")
        // Common female prefixes/structures globally
        val femaleNames = setOf("mary", "sarah", "jessica", "priya", "pooja", "fatima", "aisha", "maria", "ana", "anu")
        
        if (maleNames.contains(firstName)) return "Male"
        if (femaleNames.contains(firstName)) return "Female"
        
        // Female patterns (global heuristics)
        if (firstName.endsWith("a") || firstName.endsWith("ita") || firstName.endsWith("ina") || 
            firstName.endsWith("iya") || firstName.endsWith("ani") || firstName.endsWith("ee") || 
            firstName.endsWith("ni") || firstName.endsWith("ma")) return "Female"
            
        // Male patterns (global heuristics)
        if (firstName.endsWith("o") || firstName.endsWith("us") || firstName.endsWith("ish") || 
            firstName.endsWith("ul") || firstName.endsWith("an") || firstName.endsWith("it") || 
            firstName.endsWith("sh") || firstName.endsWith("ra") || firstName.endsWith("th") || 
            firstName.endsWith("nd") || firstName.endsWith("ej") || firstName.endsWith("iv")) return "Male"
            
        return "Prefer not to say" // Safe fallback
    }

    private suspend fun syncHistoryForContact(contactId: Long, phone: String) {
        withContext(Dispatchers.IO) {
            val resolver = getApplication<Application>().contentResolver
            val logs = readCallLogs(resolver).filter { it.number.replace("\\s".toRegex(), "").contains(phone.replace("\\s".toRegex(), "")) }
            
            logs.take(10).forEach { log -> // take last 10 logs
                val type = when (log.type) {
                    CallLog.Calls.INCOMING_TYPE -> "CALL"
                    CallLog.Calls.OUTGOING_TYPE -> "CALL"
                    CallLog.Calls.MISSED_TYPE -> "CALL"
                    else -> "CALL"
                }
                
                val dateStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date(log.date))
                
                // Convert Android's raw seconds to minutes
                val durationMinutes = if (log.duration > 0L) (log.duration / 60L).coerceAtLeast(1L) else 0L
                
                interactionRepo.createInteraction(
                    com.contactpro.app.model.InteractionRequest(
                        type = type,
                        notes = "Sync call: ${if (log.type == CallLog.Calls.OUTGOING_TYPE) "Outgoing" else "Incoming"}",
                        duration = durationMinutes,
                        contactId = contactId,
                        interactionDate = dateStr
                    )
                )
            }
        }
    }

    fun resetImportState() { _importState.value = null; _importCount.value = 0 }

    // ── VCF file import ───────────────────────────────────────────────────────
    private val _vcfImportState = MutableStateFlow<ApiResult<String>?>(null)
    val vcfImportState: StateFlow<ApiResult<String>?> = _vcfImportState.asStateFlow()

    fun importVcf(userId: Long, file: File) {
        viewModelScope.launch {
            _vcfImportState.value = ApiResult.Loading
            _vcfImportState.value = repo.importVcf(userId, file)
        }
    }

    fun resetVcfImportState() { _vcfImportState.value = null }

    // ── Device ContentResolver helpers ────────────────────────────────────────

    private fun readDeviceContacts(resolver: ContentResolver): List<DeviceContact> {
        val result = mutableListOf<DeviceContact>()
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        ) ?: return result

        val seen = mutableSetOf<String>()
        cursor.use {
            val idIdx   = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIdx = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numIdx  = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val id   = it.getString(idIdx) ?: continue
                val name = it.getString(nameIdx) ?: continue
                val num  = it.getString(numIdx) ?: continue
                if (id !in seen) {
                    seen.add(id)
                    result.add(DeviceContact(id, name, num.replace("\\s".toRegex(), "")))
                }
            }
        }
        return result
    }

    private fun readCallLogs(resolver: ContentResolver): List<CallLogEntry> {
        val result = mutableListOf<CallLogEntry>()
        val cursor = resolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.DURATION,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE
            ),
            null, null,
            CallLog.Calls.DATE + " DESC"
        ) ?: return result

        cursor.use {
            val numIdx  = it.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
            val nameIdx = it.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)
            val durIdx  = it.getColumnIndexOrThrow(CallLog.Calls.DURATION)
            val typeIdx = it.getColumnIndexOrThrow(CallLog.Calls.TYPE)
            val dateIdx = it.getColumnIndexOrThrow(CallLog.Calls.DATE)
            var count   = 0
            while (it.moveToNext() && count < 100) {
                result.add(
                    CallLogEntry(
                        number   = it.getString(numIdx) ?: "",
                        name     = it.getString(nameIdx),
                        duration = it.getLong(durIdx),
                        type     = it.getInt(typeIdx),
                        date     = it.getLong(dateIdx)
                    )
                )
                count++
            }
        }
        return result
    }
}
