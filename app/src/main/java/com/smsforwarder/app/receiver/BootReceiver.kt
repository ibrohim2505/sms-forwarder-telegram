package com.smsforwarder.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.smsforwarder.app.service.SmsForwardingService
import com.smsforwarder.app.utils.PreferenceHelper

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val preferenceHelper = PreferenceHelper(context)
            
            if (preferenceHelper.isServiceEnabled()) {
                val serviceIntent = Intent(context, SmsForwardingService::class.java)
                ContextCompat.startForegroundService(context, serviceIntent)
            }
        }
    }
}
