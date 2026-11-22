package com.example.notificationsapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.notificationsapp.data.NotificationData
import com.example.notificationsapp.data.NotificationImportance

class AppNotificationManager(context: Context) {

    private val appContext = context.applicationContext

    private val notificationManager =
        appContext.getSystemService(NotificationManager::class.java)

    private val preferences = appContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    fun createChannels() {
        val channels = listOf(
            NotificationChannel(
                CHANNEL_MIN,
                appContext.getString(R.string.channel_min_name),
                NotificationManager.IMPORTANCE_MIN
            ),
            NotificationChannel(
                CHANNEL_DEFAULT,
                appContext.getString(R.string.channel_default_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ),
            NotificationChannel(
                CHANNEL_HIGH,
                appContext.getString(R.string.channel_high_name),
                NotificationManager.IMPORTANCE_HIGH
            )
        )

        notificationManager.createNotificationChannels(channels)
    }

    fun sendNotification(data: NotificationData): Int? {
        // Разрешение проверяем непосредственно перед отправкой.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionGranted = ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!permissionGranted) {
                return null
            }
        }

        // Пользователь может отключить уведомления
        // в настройках и на Android ниже 13.
        if (!notificationManager.areNotificationsEnabled()) {
            return null
        }

        val channelId = when (data.importance) {
            NotificationImportance.MIN -> CHANNEL_MIN
            NotificationImportance.DEFAULT -> CHANNEL_DEFAULT
            NotificationImportance.HIGH -> CHANNEL_HIGH
        }

        // Проверяем, не отключён ли именно выбранный канал.
        val channel = notificationManager.getNotificationChannel(channelId)

        if (channel == null ||
            channel.importance == NotificationManager.IMPORTANCE_NONE
        ) {
            return null
        }

        val notificationId = createNotificationId()

        val notification = NotificationCompat.Builder(
            appContext,
            channelId
        )
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(data.title)
            .setContentText(data.message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(data.message)
            )
            .setOngoing(data.ongoing)
            .build()

        notificationManager.notify(
            notificationId,
            notification
        )

        return notificationId
    }

    private fun createNotificationId(): Int {
        val id = preferences.getInt(KEY_NEXT_NOTIFICATION_ID, FIRST_NOTIFICATION_ID)

        preferences.edit()
            .putInt(KEY_NEXT_NOTIFICATION_ID, id + 1)
            .apply()

        return id
    }

    private companion object {
        const val PREFERENCES_NAME = "notification_preferences"
        const val KEY_NEXT_NOTIFICATION_ID = "next_notification_id"
        const val FIRST_NOTIFICATION_ID = 1
        const val CHANNEL_MIN = "notifications_min"
        const val CHANNEL_DEFAULT = "notifications_default"
        const val CHANNEL_HIGH = "notifications_high"
    }
}