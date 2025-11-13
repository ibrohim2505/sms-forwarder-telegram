package com.smsforwarder.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.smsforwarder.app.service.SmsForwardingService
import com.smsforwarder.app.utils.PreferenceHelper

class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received broadcast: ${intent.action}")
        
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            val preferenceHelper = PreferenceHelper(context)
            
            // Always enable and start service on boot
            preferenceHelper.setServiceEnabled(true)
            
            val serviceIntent = Intent(context, SmsForwardingService::class.java)
            try {
                ContextCompat.startForegroundService(context, serviceIntent)
                Log.d(TAG, "SMS Forwarding Service started successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start service", e)
            }
        }
    }
}
