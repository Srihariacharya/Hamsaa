package com.contactpro.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.contactpro.app.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            if (state == TelephonyManager.EXTRA_STATE_IDLE) {
                // Call ended, trigger sync
                val session = SessionManager(context)
                CoroutineScope(Dispatchers.IO).launch {
                    val userId = session.userId.first()
                    if (userId != -1L) {
                        Log.d("CallReceiver", "Call ended, waiting 2s for log to stabilize...")
                        kotlinx.coroutines.delay(2000)
                        Log.d("CallReceiver", "Triggering background sync for user $userId")
                        SyncManager.syncRecentCalls(context, userId)
                    }
                }
            }
        }
    }
}
