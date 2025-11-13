package com.smsforwarder.app.telegram

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class TelegramBot(private val botToken: String) {
    
    companion object {
        private const val TAG = "TelegramBot"
        private const val BASE_URL = "https://api.telegram.org/bot"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun sendMessage(chatId: String, text: String): Boolean {
        return try {
            val url = "$BASE_URL$botToken/sendMessage"
            
            val jsonObject = JSONObject().apply {
                put("chat_id", chatId)
                put("text", text)
                put("parse_mode", "Markdown")
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonObject.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful) {
                Log.d(TAG, "Message sent successfully: $responseBody")
                true
            } else {
                Log.e(TAG, "Failed to send message. Code: ${response.code}, Body: $responseBody")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception sending message: ${e.message}", e)
            false
        }
    }

    fun testConnection(): Boolean {
        return try {
            val url = "$BASE_URL$botToken/getMe"
            
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()
            val success = response.isSuccessful
            
            Log.d(TAG, "Bot connection test: ${if (success) "SUCCESS" else "FAILED"}")
            success
        } catch (e: Exception) {
            Log.e(TAG, "Exception testing connection: ${e.message}", e)
            false
        }
    }
}
