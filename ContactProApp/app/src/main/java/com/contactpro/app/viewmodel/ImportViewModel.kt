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
                if (realContacts.isEmpty()) {
                    val firstNamesM = listOf("Arjun", "Rahul", "Vikram", "Aditya", "Rohan", "Karan", "Siddharth", "Amit", "Manish", "Suresh", "Ravi", "Sanjay")
                    val firstNamesF = listOf("Priya", "Sneha", "Ananya", "Riya", "Neha", "Pooja", "Aarti", "Kavita", "Swati", "Nidhi", "Divya", "Kriti")
                    val lastNames = listOf("Sharma", "Verma", "Patel", "Iyer", "Nair", "Singh", "Kumar", "Gupta", "Reddy", "Joshi", "Desai", "Menon")
                    
                    (1..15).map { i ->
                        val isMale = Math.random() > 0.5
                        val firstName = if (isMale) firstNamesM.random() else firstNamesF.random()
                        val lastName = lastNames.random()
                        val genderStr = if (isMale) "Male" else "Female"
                        val phoneNum = (7000000000L..9999999999L).random().toString()
                        
                        DeviceContact(
                            id = "dummy_$i",
                            name = "$firstName $lastName",
                            phone = "+91 $phoneNum",
                            gender = genderStr
                        )
                    }
                } else {
                    realContacts
                }
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
            selected.forEach { dc ->
                // Smart Category Detection
                val detectedCategory = detectCategoryFromName(dc.name)
                
                val result = repo.createContact(
                    ContactRequest(
                        name   = dc.name,
                        phone  = dc.phone,
                        email  = null,
                        category = detectedCategory, // Automatically detected or null
                        notes  = "Synced from phone",
                        gender = dc.gender ?: "Others",
                        dob    = null,
                        followUpFrequency = 7, // default 7 days
                        userId = userId
                    )
                )
                if (result is ApiResult.Success) {
                    count++
                    val contactId = result.data.id
                    // Now sync history for this contact
                    syncHistoryForContact(contactId, dc.phone)
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
                
                interactionRepo.createInteraction(
                    com.contactpro.app.model.InteractionRequest(
                        type = type,
                        notes = "Sync call: ${if (log.type == CallLog.Calls.OUTGOING_TYPE) "Outgoing" else "Incoming"}",
                        duration = log.duration,
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
