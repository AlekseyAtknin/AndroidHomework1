package com.example.notificationsapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.notificationsapp.data.NotificationData
import com.example.notificationsapp.ui.theme.NotificationsAppTheme

class MainActivity : ComponentActivity() {

    private var hasNotificationPermission by mutableStateOf(false)
    private val appNotificationManager by lazy {
        AppNotificationManager(applicationContext)
    }

    // Получаем результат системного запроса разрешения.
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        appNotificationManager.createChannels()

        hasNotificationPermission = checkNotificationPermission()

        setContent {
            NotificationsAppTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    if (hasNotificationPermission) {
                        NotificationFormScreen(
                            onSendNotification = { data ->
                                sendNotification(data)
                            },
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                        )
                    } else {
                        NotificationPermissionScreen(
                            onOpenSettings = {
                                openNotificationSettings()
                            },
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                        )
                    }
                }
            }
        }

        requestNotificationPermissionIfNeeded()
    }

    override fun onResume() {
        super.onResume()

        // Обновляем состояние после возвращения из настроек.
        hasNotificationPermission = checkNotificationPermission()
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // По заданию на Android ниже 13 сразу показываем форму.
            true
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        if (hasNotificationPermission) {
            return
        }

        val preferences = getSharedPreferences(
            PERMISSION_PREFERENCES_NAME,
            MODE_PRIVATE
        )

        val alreadyRequested = preferences.getBoolean(
            KEY_NOTIFICATIONS_REQUESTED,
            false
        )

        if (!alreadyRequested) {
            preferences.edit()
                .putBoolean(KEY_NOTIFICATIONS_REQUESTED, true)
                .apply()

            permissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    private fun openNotificationSettings() {
        val intent = Intent(
            Settings.ACTION_APP_NOTIFICATION_SETTINGS
        ).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }

        startActivity(intent)
    }


    private fun sendNotification(data: NotificationData) {
        val notificationId = appNotificationManager.sendNotification(data)

        val message = if (notificationId != null) {
            getString(R.string.notification_sent, notificationId)
        } else {
            getString(R.string.notifications_disabled)
        }

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
    private companion object {
        const val PERMISSION_PREFERENCES_NAME = "permission_preferences"
        const val KEY_NOTIFICATIONS_REQUESTED = "notifications_requested"
    }
}