package com.tapspend.app.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.tapspend.app.R
import com.tapspend.app.domain.formatPkr
import com.tapspend.app.ui.MainActivity
import com.tapspend.app.ui.QuickAddActivity

object NotificationHelper {
    const val CHANNEL_ID = "tapspend_persistent_channel"
    const val NOTIFICATION_ID = 1001

    private const val REQUEST_CODE_OPEN_APP = 0
    private const val REQUEST_CODE_QUICK_ADD = 1

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "TapSpend Quick Tracker"
            val descriptionText = "Always-available quick expense recording notification"
            val importance = NotificationManager.IMPORTANCE_LOW // Low ensures no sound spam
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /** True when notifications may actually be posted on this device/OS version. */
    fun canPostNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Shows/updates the ongoing notification that carries today's total and the
     * one-tap [+ Add Expense] action. Silently does nothing without permission.
     */
    fun showPersistentNotification(context: Context, todaySpentPKR: Double) {
        if (!canPostNotifications(context)) return

        createNotificationChannel(context)

        // PendingIntent to open the full app dashboard
        val appIntent = Intent(context, MainActivity::class.java)
        val appPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN_APP,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // PendingIntent for the [+ Add Expense] action: floating entry, no app launch
        val quickAddIntent = Intent(context, QuickAddActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val quickAddPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_QUICK_ADD,
            quickAddIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedToday = formatPkr(todaySpentPKR)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle("💰 TapSpend · Today: $formattedToday")
            .setContentText("Quickly record your expense")
            .setOngoing(true) // Persistent notification
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(appPendingIntent)
            .addAction(
                R.drawable.ic_add,
                "+ Add Expense",
                quickAddPendingIntent
            )
            .setAutoCancel(false)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun cancelNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
