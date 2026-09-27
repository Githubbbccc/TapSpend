package com.tapspend.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.tapspend.app.R
import com.tapspend.app.ui.MainActivity

object NotificationHelper {
    const val CHANNEL_ID = "tapspend_persistent_channel"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "TapSpend Quick Tracker"
            val descriptionText = "Always-available quick expense recording notification"
            val importance = NotificationManager.IMPORTANCE_LOW // Low ensures no sound spam
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showPersistentNotification(context: Context, todaySpentPKR: Double) {
        createNotificationChannel(context)

        // PendingIntent to Open Full App Dashboard
        val appIntent = Intent(context, MainActivity::class.java)
        val appPendingIntent = PendingIntent.getActivity(
            context,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // PendingIntent for [+ Add Expense] Action
        val quickAddIntent = Intent(context, MainActivity::class.java).apply {
            action = "com.tapspend.app.ACTION_QUICK_ADD"
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val quickAddPendingIntent = PendingIntent.getActivity(
            context,
            1,
            quickAddIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedToday = "Rs %,.0f".format(todaySpentPKR)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle("💰 TapSpend · Today: $formattedToday")
            .setContentText("Quickly record your expense")
            .setOngoing(true) // Persistent Notification
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(appPendingIntent)
            .addAction(
                R.drawable.ic_add,
                "+ Add Expense",
                quickAddPendingIntent
            )
            .setAutoCancel(false)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun cancelNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
