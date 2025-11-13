package com.smsforwarder.app.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceHelper(context: Context) {
    
    companion object {
        private const val PREF_NAME = "SmsForwarderPrefs"
        private const val KEY_BOT_TOKEN = "bot_token"
        private const val KEY_CHAT_ID = "chat_id"
        private const val KEY_SERVICE_ENABLED = "service_enabled"
        private const val KEY_APP_CONFIGURED = "app_configured"
    }

    private val preferences: SharedPreferences = 
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveBotToken(token: String) {
        preferences.edit().putString(KEY_BOT_TOKEN, token).apply()
    }

    fun getBotToken(): String {
        return preferences.getString(KEY_BOT_TOKEN, "") ?: ""
    }

    fun saveChatId(chatId: String) {
        preferences.edit().putString(KEY_CHAT_ID, chatId).apply()
    }

    fun getChatId(): String {
        return preferences.getString(KEY_CHAT_ID, "") ?: ""
    }

    fun setServiceEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_SERVICE_ENABLED, enabled).apply()
    }

    fun isServiceEnabled(): Boolean {
        return preferences.getBoolean(KEY_SERVICE_ENABLED, false)
    }

    fun setAppConfigured(configured: Boolean) {
        preferences.edit().putBoolean(KEY_APP_CONFIGURED, configured).apply()
    }

    fun isAppConfigured(): Boolean {
        return preferences.getBoolean(KEY_APP_CONFIGURED, false)
    }
}
