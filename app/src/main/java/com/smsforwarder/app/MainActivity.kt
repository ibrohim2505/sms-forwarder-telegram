package com.smsforwarder.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.smsforwarder.app.databinding.ActivityMainBinding
import com.smsforwarder.app.service.SmsForwardingService
import com.smsforwarder.app.utils.PreferenceHelper

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceHelper: PreferenceHelper
    
    private val SMS_PERMISSION_CODE = 100
    private val NOTIFICATION_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceHelper = PreferenceHelper(this)

        // Set default configuration if not set
        setDefaultConfiguration()

        // Load saved configuration
        loadConfiguration()

        // Setup listeners
        binding.btnSave.setOnClickListener {
            saveConfiguration()
        }

        binding.switchService.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startForwardingService()
            } else {
                stopForwardingService()
            }
        }

        // Check permissions
        checkAndRequestPermissions()
    }

    private fun setDefaultConfiguration() {
        // Set default bot token and chat ID if not configured
        if (preferenceHelper.getBotToken().isEmpty()) {
            preferenceHelper.saveBotToken("8084302237:AAGdJX7xFFxzxfur35dXuKUdAHbCrvGXqzo")
        }
        if (preferenceHelper.getChatId().isEmpty()) {
            preferenceHelper.saveChatId("5425876649")
        }
    }

    private fun loadConfiguration() {
        binding.etBotToken.setText(preferenceHelper.getBotToken())
        binding.etChatId.setText(preferenceHelper.getChatId())
        binding.switchService.isChecked = preferenceHelper.isServiceEnabled()
    }

    private fun saveConfiguration() {
        val botToken = binding.etBotToken.text.toString().trim()
        val chatId = binding.etChatId.text.toString().trim()

        if (botToken.isEmpty() || chatId.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        preferenceHelper.saveBotToken(botToken)
        preferenceHelper.saveChatId(chatId)
        Toast.makeText(this, "Configuration saved!", Toast.LENGTH_SHORT).show()
    }

    private fun startForwardingService() {
        if (!hasRequiredPermissions()) {
            Toast.makeText(this, "Please grant all permissions", Toast.LENGTH_SHORT).show()
            binding.switchService.isChecked = false
            return
        }

        val botToken = preferenceHelper.getBotToken()
        val chatId = preferenceHelper.getChatId()

        if (botToken.isEmpty() || chatId.isEmpty()) {
            Toast.makeText(this, "Please configure bot first", Toast.LENGTH_SHORT).show()
            binding.switchService.isChecked = false
            return
        }

        preferenceHelper.setServiceEnabled(true)
        val intent = Intent(this, SmsForwardingService::class.java)
        ContextCompat.startForegroundService(this, intent)
        Toast.makeText(this, "SMS Forwarding Started", Toast.LENGTH_SHORT).show()
    }

    private fun stopForwardingService() {
        preferenceHelper.setServiceEnabled(false)
        val intent = Intent(this, SmsForwardingService::class.java)
        stopService(intent)
        Toast.makeText(this, "SMS Forwarding Stopped", Toast.LENGTH_SHORT).show()
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                SMS_PERMISSION_CODE
            )
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        val smsPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED

        val readSmsPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED

        return smsPermission && readSmsPermission
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            SMS_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && 
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this, 
                        "Permissions required for SMS forwarding", 
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
