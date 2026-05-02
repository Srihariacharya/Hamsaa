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
                
                // 1. Fetch backend contacts
                val contactsResult = contactRepo.getContacts(userId)
                val backendContacts = if (contactsResult is ApiResult.Success) contactsResult.data else emptyList()
                if (backendContacts.isEmpty()) return@withContext
                
                // 2. Fetch ALL existing interactions once
                val interactionsResult = interactionRepo.getInteractionsByUser(userId)
                val allExistingInteractions = if (interactionsResult is ApiResult.Success) interactionsResult.data else emptyList()
                
                // Use a normalized timestamp (no T, no spaces) to prevent string mismatch
                val existingTimestampsByContact = allExistingInteractions.groupBy(
                    { it.contactId }, 
                    { it.interactionDate.replace("[^0-9]".toRegex(), "").take(12) } // yyyyMMddHHmm
                )
                
                // 3. Read phone logs
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
                        
                        // Duplicate check
                        if (existingSet.contains(normalizedLogTime)) continue
                        
                        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.date))
                        newInteractions.add(
                            InteractionRequest(
                                type = "CALL",
                                notes = "Sync call: ${if (log.type == CallLog.Calls.OUTGOING_TYPE) "Outgoing" else "Incoming"} at $timeStr",
                                duration = log.duration.coerceAtMost(7200L),
                                contactId = contact.id,
                                interactionDate = dateISO
                            )
                        )
                    }
                }
                
                // 4. BATCH SYNC: Upload everything in chunks of 50 to prevent timeouts
                if (newInteractions.isNotEmpty()) {
                    newInteractions.chunked(50).forEach { chunk ->
                        interactionRepo.createInteractionsBatch(chunk)
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
