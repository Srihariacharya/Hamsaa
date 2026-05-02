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
                
                // 1. Fetch all backend contacts
                val contactsResult = contactRepo.getContacts(userId)
                val backendContacts = if (contactsResult is ApiResult.Success) contactsResult.data else emptyList()
                if (backendContacts.isEmpty()) {
                    isSyncing = false
                    return@withContext
                }
                
                // 2. Fetch ALL existing interactions for this user in ONE call (Highly Optimized)
                val interactionsResult = interactionRepo.getInteractionsByUser(userId)
                val allExistingInteractions = if (interactionsResult is ApiResult.Success) interactionsResult.data else emptyList()
                
                // Group existing interactions by contactId for fast lookup
                val existingTimestampsByContact = allExistingInteractions.groupBy({ it.id }, { it.interactionDate.substring(0, 19) })
                
                // 3. Fetch all call logs from phone
                val logs = readCallLogs(context)
                val logsByNumber = logs.groupBy { normalizePhone(it.number) }
                
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                
                backendContacts.forEach { contact ->
                    val contactLogs = logsByNumber[normalizePhone(contact.phone)] ?: return@forEach
                    
                    val existingTimestamps = existingTimestampsByContact[contact.id]?.toSet() ?: emptySet()
                    
                    contactLogs.forEach { log ->
                        val dateStrISO = sdf.format(Date(log.date))
                        
                        // Check if this exact call (by timestamp) already exists locally (no network call needed)
                        if (existingTimestamps.contains(dateStrISO)) return@forEach
                        
                        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.date))
                        val durationSeconds = log.duration.coerceAtMost(7200L)
                        
                        // Only create if it's new
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
