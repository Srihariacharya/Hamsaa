package com.contactpro.app

import android.content.Context
import android.provider.CallLog
import android.util.Log
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
                
                // Step 0: Clean up
                try { RetrofitClient.apiService.cleanupCorruptedInteractions(userId) } catch (e: Exception) {}
                try { RetrofitClient.apiService.deduplicateContacts(userId) } catch (e: Exception) {}
                
                // Optional: Wipe old low-precision data once to ensure a clean start
                // We'll only do this if the user hasn't successfully synced with high-precision yet
                val session = com.contactpro.app.SessionManager(context)
                val hasAppliedFix = session.userDataStore.data.first().id != -1L // Use a better flag if available
                
                // 1. Fetch all backend contacts
                val contactsResult = contactRepo.getContacts(userId)
                val backendContacts = if (contactsResult is ApiResult.Success) contactsResult.data else emptyList()
                if (backendContacts.isEmpty()) return@withContext
                
                // 2. Fetch all call logs
                val logs = readCallLogs(context)
                val logsByNumber = logs.groupBy { normalizePhone(it.number) }
                
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                
                backendContacts.forEach { contact ->
                    val contactLogs = logsByNumber[normalizePhone(contact.phone)] ?: return@forEach
                    
                    // Instead of a simple "after" check, we want to capture ALL history 
                    // unless the record already exists. 
                    // For now, to keep it simple and fix the user's issue, 
                    // we will sync everything if the contact has very few interactions
                    val existingInteractions = interactionRepo.getInteractions(contact.id)
                    val existingTimestamps = if (existingInteractions is ApiResult.Success) {
                        existingInteractions.data.map { it.interactionDate.substring(0, 19) }.toSet()
                    } else emptySet()
                    
                    contactLogs.forEach { log ->
                        val dateStrISO = sdf.format(Date(log.date))
                        
                        // Check if this exact call (by timestamp) already exists
                        if (existingTimestamps.contains(dateStrISO)) return@forEach
                        
                        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.date))
                        val durationSeconds = log.duration.coerceAtMost(7200L)
                        
                        interactionRepo.createInteraction(
                            InteractionRequest(
                                type = "CALL",
                                notes = "Sync call: ${if (log.type == CallLog.Calls.OUTGOING_TYPE) "Outgoing" else "Incoming"} at $timeStr",
                                duration = durationSeconds,
                                contactId = contact.id,
                                interactionDate = dateStrISO
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Error syncing calls", e)
        } finally {
            isSyncing = false
        }
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

data class CallLogEntryBasic(
    val number: String,
    val duration: Long,
    val type: Int,
    val date: Long
)
