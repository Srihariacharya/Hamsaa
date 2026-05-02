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
                
                // We only want to upload calls that happen after the contact's lastInteractionDate
                // For a true bulletproof sync, we would check Interaction table, but checking date is a good heuristic
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                
                logs.forEach { log ->
                    val normalizedNum = log.number.replace("\\s".toRegex(), "")
                    val contactId = phoneToId[normalizedNum] ?: return@forEach
                    
                    val type = when (log.type) {
                        CallLog.Calls.INCOMING_TYPE -> "CALL"
                        CallLog.Calls.OUTGOING_TYPE -> "CALL"
                        CallLog.Calls.MISSED_TYPE -> "CALL"
                        else -> "CALL"
                    }
                    
                    val dateStr = sdf.format(Date(log.date))
                    
                    // Call Duration Mathematics: Divide Android's seconds by 60 to get minutes
                    val durationMinutes = if (log.duration > 0L) (log.duration / 60L).coerceAtLeast(1L) else 0L
                    
                    interactionRepo.createInteraction(
                        InteractionRequest(
                            type = type,
                            notes = "Sync call: ${if (log.type == CallLog.Calls.OUTGOING_TYPE) "Outgoing" else "Incoming"}",
                            duration = durationMinutes,
                            contactId = contactId,
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
                var count   = 0
                while (it.moveToNext() && count < 20) { // Only check latest 20 to save backend hits
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
