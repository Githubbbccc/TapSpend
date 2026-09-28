package com.tapspend.app.notification

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.tapspend.app.TapSpendApplication
import com.tapspend.app.domain.DateRanges
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Restores the always-available quick-add notification after the device reboots,
 * so the 3-second capture flow survives restarts.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val app = context.applicationContext as? TapSpendApplication ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notificationsEnabled = app.preferences.notificationEnabledFlow.first()
                if (!notificationsEnabled) return@launch

                val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                if (!permissionGranted) return@launch

                val todayTotal = app.database
                    .expenseDao()
                    .getTodayTotal(DateRanges.startOfToday())
                    .first()

                NotificationHelper.showPersistentNotification(context, todayTotal)
            } catch (error: Exception) {
                // Never crash the device boot: the notification is a convenience, not a requirement.
                Log.w(TAG, "Could not restore the TapSpend notification", error)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        const val TAG = "TapSpendBoot"
    }
}
