package com.contactpro.app

import android.content.Context
import android.provider.CallLog
import android.provider.ContactsContract
import android.util.Log
import com.contactpro.app.model.ContactRequest
import com.contactpro.app.model.InteractionRequest
import com.contactpro.app.network.ApiResult
import com.contactpro.app.network.RetrofitClient
import com.contactpro.app.repository.ContactRepository
import com.contactpro.app.repository.InteractionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SyncManager {

    private var isSyncing = false

    private fun normalizePhone(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        return if (digits.length >= 10) digits.takeLast(10) else digits
    }

    suspend fun syncRecentCalls(context: Context, userId: Long) {
        if (isSyncing) return
        isSyncing = true
        
        try {
            withContext(Dispatchers.IO) {
                val contactRepo = ContactRepository(RetrofitClient.apiService)
                val interactionRepo = InteractionRepository(RetrofitClient.apiService)
                
                // STEP 1: Auto-import NEW contacts from the phone
                // This ensures "No History" contacts are actually in the system
                val deviceContacts = readDeviceContacts(context)
                if (deviceContacts.isNotEmpty()) {
                    val batchRequests = deviceContacts.map {
                        ContactRequest(
                            name = it.name,
                            phone = it.phone,
                            email = null,
                            category = "Personal",
                            notes = "Auto-synced from device",
                            gender = com.contactpro.app.util.GenderPredictor.predict(it.name),
                            dob = null,
                            userId = userId
                        )
                    }
                    // Upload in chunks of 100 to handle large address books
                    batchRequests.chunked(100).forEach { chunk ->
                        contactRepo.createContactsBatch(chunk)
                    }
                }

                // STEP 2: Fetch all backend contacts (now including the newly imported ones)
                val contactsResult = contactRepo.getContacts(userId)
                val backendContacts = if (contactsResult is ApiResult.Success) contactsResult.data else emptyList()
                if (backendContacts.isEmpty()) return@withContext
                
                // STEP 3: Fetch ALL existing interactions once to prevent duplicates
                val interactionsResult = interactionRepo.getInteractionsByUser(userId)
                val allExistingInteractions = if (interactionsResult is ApiResult.Success) interactionsResult.data else emptyList()
                val existingTimestampsByContact = allExistingInteractions.groupBy(
                    { it.contactId }, 
                    { it.interactionDate.replace("[^0-9]".toRegex(), "").take(12) }
                )
                
                // STEP 4: Read full call history from phone logs
                val logs = readCallLogs(context)
                val logsByNumber = logs.groupBy { normalizePhone(it.number) }
                
                val sdfISO = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val newInteractions = mutableListOf<InteractionRequest>()
                
                for (contact in backendContacts) {
                    val contactLogs = logsByNumber[normalizePhone(contact.phone)] ?: continue
                    val existingSet = existingTimestampsByContact[contact.id]?.toSet() ?: emptySet()
                    
                    for (log in contactLogs) {
                        val dateISO = sdfISO.format(Date(log.date))
                        val normalizedLogTime = dateISO.replace("[^0-9]".toRegex(), "").take(12)
                        
                        if (existingSet.contains(normalizedLogTime)) continue
                        
                        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.date))
                        newInteractions.add(
                            InteractionRequest(
                                type = "CALL",
                                notes = "Sync call: ${if (log.type == CallLog.Calls.OUTGOING_TYPE) "Outgoing" else "Incoming"} at $timeStr",
                                duration = log.duration, // Raw seconds
                                contactId = contact.id,
                                interactionDate = dateISO
                            )
                        )
                    }
                }
                
                // STEP 5: Batch upload interaction history in chunks of 50
                if (newInteractions.isNotEmpty()) {
                    Log.d("SyncManager", "Uploading ${newInteractions.size} new interactions")
                    newInteractions.chunked(50).forEach { chunk ->
                        interactionRepo.createInteractionsBatch(chunk)
                    }
                }
                
                // STEP 6: Clean up any duplicates that slipped through
                try {
                    RetrofitClient.apiService.deduplicateInteractions(userId)
                } catch (e: Exception) {
                    Log.w("SyncManager", "Dedup call failed (non-critical)", e)
                }
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Error during full sync", e)
        } finally {
            isSyncing = false
        }
    }

    private fun readDeviceContacts(context: Context): List<DeviceContactBasic> {
        val result = mutableListOf<DeviceContactBasic>()
        try {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
                null, null, null
            )
            cursor?.use {
                val nameIdx = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx  = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (it.moveToNext()) {
                    val name = it.getString(nameIdx) ?: "Unknown"
                    val phone = it.getString(numIdx) ?: ""
                    if (phone.isNotBlank()) {
                        result.add(DeviceContactBasic(name, phone))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Error reading contacts", e)
        }
        return result
    }

    private fun readCallLogs(context: Context): List<CallLogEntryBasic> {
        val result = mutableListOf<CallLogEntryBasic>()
        try {
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DURATION, CallLog.Calls.TYPE, CallLog.Calls.DATE),
                null, null, CallLog.Calls.DATE + " DESC"
            ) ?: return result

            cursor.use {
                val numIdx  = it.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
                val durIdx  = it.getColumnIndexOrThrow(CallLog.Calls.DURATION)
                val typeIdx = it.getColumnIndexOrThrow(CallLog.Calls.TYPE)
                val dateIdx = it.getColumnIndexOrThrow(CallLog.Calls.DATE)
                while (it.moveToNext()) {
                    result.add(
                        CallLogEntryBasic(
                            number   = it.getString(numIdx) ?: "",
                            duration = it.getLong(durIdx),
                            type     = it.getInt(typeIdx),
                            date     = it.getLong(dateIdx)
                        )
                    )
                }
            }
        } catch (e: Exception) {}
        return result
    }
}

data class DeviceContactBasic(val name: String, val phone: String)
data class CallLogEntryBasic(val number: String, val duration: Long, val type: Int, val date: Long)
