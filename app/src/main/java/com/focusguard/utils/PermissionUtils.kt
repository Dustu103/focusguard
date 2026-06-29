package com.focusguard.utils

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.app.admin.DevicePolicyManager
import android.app.usage.UsageStatsManager

object PermissionUtils {
    fun isAccessibilityEnabled(context: Context): Boolean {
        val service = "${context.packageName}/com.focusguard.services.FocusAccessibilityService"
        return try {
            val enabled = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            enabled.split(":").any { comp -> comp.equals(service, ignoreCase = true) }
        } catch (e: Exception) { false }
    }

    fun isDeviceAdminActive(context: Context): Boolean {
        return try {
            val dpm = context.applicationContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val comp = ComponentName("com.focusguard", "com.focusguard.admin.DeviceAdminReceiver")
            val isActive = dpm.isAdminActive(comp)
            android.util.Log.d("DeviceAdmin", "isDeviceAdminActive check returned: $isActive")
            isActive
        } catch (e: Exception) {
            android.util.Log.e("DeviceAdmin", "Error checking admin status: ${e.message}")
            false
        }
    }

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }
}
