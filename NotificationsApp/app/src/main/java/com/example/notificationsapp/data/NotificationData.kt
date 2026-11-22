package com.example.notificationsapp.data

import androidx.annotation.StringRes
import com.example.notificationsapp.R

enum class NotificationImportance(@StringRes val labelRes: Int) {
    MIN(R.string.importance_min),
    DEFAULT(R.string.importance_default),
    HIGH(R.string.importance_high)
}

data class NotificationData(
    val title: String,
    val message: String,
    val ongoing: Boolean,
    val importance: NotificationImportance
)