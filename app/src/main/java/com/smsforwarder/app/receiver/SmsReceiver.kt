package com.smsforwarder.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.smsforwarder.app.telegram.TelegramBot
import com.smsforwarder.app.utils.PreferenceHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "SmsReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        val preferenceHelper = PreferenceHelper(context)
        
        if (!preferenceHelper.isServiceEnabled()) {
            Log.d(TAG, "Service is disabled, ignoring SMS")
            return
        }

        val messages = extractSmsMessages(intent)
        if (messages.isEmpty()) {
            Log.d(TAG, "No messages extracted")
            return
        }

        messages.forEach { smsMessage ->
            val sender = smsMessage.displayOriginatingAddress
            val messageBody = smsMessage.messageBody
            
            Log.d(TAG, "SMS received from: $sender")
            
            // Forward to Telegram
            forwardToTelegram(context, sender, messageBody)
        }
    }

    private fun extractSmsMessages(intent: Intent): List<SmsMessage> {
        val messages = mutableListOf<SmsMessage>()
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Telephony.Sms.Intents.getMessagesFromIntent(intent)?.let {
                    messages.addAll(it)
                }
            } else {
                val pdus = intent.extras?.get("pdus") as? Array<*>
                pdus?.forEach { pdu ->
                    @Suppress("DEPRECATION")
                    val message = SmsMessage.createFromPdu(pdu as ByteArray)
                    messages.add(message)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting SMS: ${e.message}", e)
        }
        
        return messages
    }

    private fun forwardToTelegram(context: Context, sender: String, message: String) {
        val preferenceHelper = PreferenceHelper(context)
        val botToken = preferenceHelper.getBotToken()
        val chatId = preferenceHelper.getChatId()

        if (botToken.isEmpty() || chatId.isEmpty()) {
            Log.w(TAG, "Bot configuration not found")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val telegramBot = TelegramBot(botToken)
                val formattedMessage = "📱 *SMS Received*\n\n" +
                        "*From:* $sender\n" +
                        "*Message:* $message"
                
                val success = telegramBot.sendMessage(chatId, formattedMessage)
                if (success) {
                    Log.d(TAG, "SMS forwarded successfully")
                } else {
                    Log.e(TAG, "Failed to forward SMS")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error forwarding to Telegram: ${e.message}", e)
            }
        }
    }
}
