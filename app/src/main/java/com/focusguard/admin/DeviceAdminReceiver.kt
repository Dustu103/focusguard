package com.focusguard.admin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat

class DeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.d("DeviceAdmin", "Device admin enabled — protection active")
    }

    /**
     * Called when the user reaches the system "Deactivate this admin?" dialog.
     * 
     * NOTE: Our Accessibility Service already intercepts the Device Admin settings 
     * screen BEFORE the user reaches this point — requiring a PIN gate. This method 
     * is now a SECOND layer of defense in case accessibility is off or was bypassed.
     *
     * Strategy: try direct Activity launch first (works if the app has a recent 
     * foreground window or SYSTEM_ALERT_WINDOW), fall back to notification.
     */
    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        val pinIntent = Intent(context, com.focusguard.ui.screens.ChildBlockActivity::class.java).apply {
            putExtra("is_device_admin_disable", true)
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_NO_HISTORY
            )
        }
        // Try direct launch first — works if app recently had a foreground window
        try {
            context.startActivity(pinIntent)
        } catch (e: Exception) {
            // Fallback: show high-priority notification
            Log.w("DeviceAdmin", "Direct launch failed, using notification: ${e.message}")
            showPinNotification(context)
        }
        return "⚠️ FocusGuard is active. Enter your Parent PIN to deactivate protection."
    }


    private fun showPinNotification(context: Context) {
        val channelId = "focusguard_admin_pin"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "FocusGuard PIN Verification",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Used to verify parent PIN before deactivating protection"
        }
        nm.createNotificationChannel(channel)

        val pinIntent = Intent(context, com.focusguard.ui.screens.ChildBlockActivity::class.java).apply {
            putExtra("is_device_admin_disable", true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            9999,
            pinIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("⚠️ FocusGuard Protection Active")
            .setContentText("Tap here to enter your Parent PIN before deactivating.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_lock_lock,
                "Enter PIN Now",
                pendingIntent
            )
            .build()

        nm.notify(9998, notification)
    }

    /**
     * Called AFTER admin has been disabled (e.g. user force-deactivated via Settings).
     * Immediately re-request Device Admin so protection is restored.
     */
    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.w("DeviceAdmin", "Admin disabled")
        // Note: We deliberately do NOT auto-restart the Device Admin request here.
        // If the user intentionally removed it via the app's Settings screen, 
        // popping it back up immediately creates an infinite loop.
    }
}
