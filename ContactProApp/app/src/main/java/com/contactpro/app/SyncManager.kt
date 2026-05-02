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

    suspend fun syncRecentCalls(context: Context, userId: Long) {
        if (isSyncing) return
        isSyncing = true
        
        try {
            withContext(Dispatchers.IO) {
                val contactRepo = ContactRepository(RetrofitClient.apiService)
                val interactionRepo = InteractionRepository(RetrofitClient.apiService)
                
                // Step 0: Clean up corrupted data from previous bugs
                try {
                    RetrofitClient.apiService.cleanupCorruptedInteractions(userId)
                } catch (e: Exception) {
                    Log.w("SyncManager", "Interaction cleanup failed", e)
                }
                try {
                    RetrofitClient.apiService.deduplicateContacts(userId)
                } catch (e: Exception) {
                    Log.w("SyncManager", "Contact deduplication failed", e)
                }
                
                // 1. Fetch all backend contacts for user
                val result = contactRepo.getContacts(userId)
                val backendContacts = if (result is ApiResult.Success) result.data else emptyList()
                if (backendContacts.isEmpty()) return@withContext
                
                // Map of normalized phone -> contact ID
                val phoneToId = backendContacts.associateBy(
                    { it.phone.replace("\\s".toRegex(), "") }, 
                    { it.id }
                )
                
                // 2. Fetch recent call logs (last 50 to cover any new ones since app was last open)
                val logs = readCallLogs(context)
                
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val sdfShort = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                
                logs.forEach { log ->
                    val logDate = log.date
                    // Better normalization: filter all non-digits and take last 10
                    val logNum = log.number.filter { it.isDigit() }.takeLast(10)
                    if (logNum.isEmpty()) return@forEach
                    
                    val contact = backendContacts.find { 
                        it.phone.filter { char -> char.isDigit() }.takeLast(10) == logNum 
                    } ?: return@forEach
                    
                    val lastInteractionTime = contact.lastInteractionDate?.let {
                        try { sdf.parse(it)?.time } catch (e: Exception) { 
                            try { sdfShort.parse(it)?.time } catch (e: Exception) { null }
                        }
                    } ?: 0L
                    
                    // ONLY sync if the call happened AFTER the last interaction we have on record
                    if (logDate <= lastInteractionTime) return@forEach
                    
                    val dateStr = sdf.format(Date(logDate))
                    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(logDate))
                    
                    // Use raw seconds for higher precision. 
                    // Safety cap: Never sync a call longer than 7200 seconds (2 hours).
                    val durationSeconds = log.duration.coerceAtMost(7200L)
                    
                    interactionRepo.createInteraction(
                        InteractionRequest(
                            type = "CALL",
                            notes = "Sync call: ${if (log.type == CallLog.Calls.OUTGOING_TYPE) "Outgoing" else "Incoming"} at $timeStr",
                            duration = durationSeconds,
                            contactId = contact.id,
                            interactionDate = dateStr
                        )
                    )
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
                while (it.moveToNext()) { // Sync ALL available history
                    result.add(
                        CallLogEntryBasic(
                            number   = it.getString(numIdx) ?: "",
                            duration = it.getLong(durIdx),
                            type     = it.getInt(typeIdx),
                            date     = it.getLong(dateIdx)
                        )
                    )
                    count++
                }
            }
        } catch (e: SecurityException) {
            // Permission not granted
        }
        return result
    }
}

data class CallLogEntryBasic(
    val number: String,
    val duration: Long,
    val type: Int,
    val date: Long
)
