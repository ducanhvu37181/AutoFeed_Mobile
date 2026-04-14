package com.example.autofeedmobile.ui.notification

import com.example.autofeedmobile.MainActivity


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {
    private const val CHANNEL_ID = "autofeed_notifications"
    private const val CHANNEL_NAME = "AutoFeed Notifications"
    private const val CHANNEL_DESC = "Notifications for schedules, inventory, and reports"

    private const val PREFS_NAME = "notification_prefs_v2"

    /**
     * Checks if we should notify for a given key.
     * Prevents "spam" by ensuring the same item/status doesn't notify twice in the same minute.
     * For status changes, we generally only notify once ever per state.
     */
    fun shouldNotify(context: Context, key: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastNotified = prefs.getLong(key, 0L)
        val now = System.currentTimeMillis()

        // If never notified, we should notify.
        if (lastNotified == 0L) return true

        // Spam protection: Ensure the same item/status doesn't notify twice in the same minute.
        if (now - lastNotified < 60000) {
            return false
        }

        // For most of our current keys (status-specific like "req_123_approved"), 
        // we only want to notify ONCE ever. If it's already been notified (lastNotified > 0)
        // and we reached here, it means more than a minute has passed. 
        // We still return false to avoid repeated notifications for the same persistent state.
        return false
    }

    fun markAsNotified(context: Context, key: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(key, System.currentTimeMillis()).apply()
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, title: String, message: String, notificationId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }
}
