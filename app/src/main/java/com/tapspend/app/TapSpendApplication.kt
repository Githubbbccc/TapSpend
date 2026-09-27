package com.tapspend.app

import android.app.Application
import com.tapspend.app.data.AppDatabase
import com.tapspend.app.data.AppPreferences
import com.tapspend.app.notification.NotificationHelper

class TapSpendApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val preferences by lazy { AppPreferences(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
