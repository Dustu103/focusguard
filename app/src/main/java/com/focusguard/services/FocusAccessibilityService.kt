package com.focusguard.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.focusguard.data.AppDatabase
import com.focusguard.data.PinManager
import com.focusguard.ui.screens.ChildBlockActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class FocusAccessibilityService : AccessibilityService() {

    private lateinit var db: AppDatabase
    private lateinit var pinManager: PinManager
    private var blockedPackages = setOf<String>()

    // Managed scope — cancelled in onDestroy to prevent leaks
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // --- Settings screens a child could use to bypass protection ---
    // These package + class name patterns are matched case-insensitively.
    // Covers AOSP, Samsung, MIUI, and other OEM variants.
    private val sensitiveSettingsPatterns = listOf(
        // Device Admin deactivation screen
        "deviceadmin",
        "device_admin",
        // Accessibility settings (child could turn off our service)
        "accessibilitysettings",
        "accessibility_settings",
        // App info / uninstall screen for our own package
        "com.focusguard"   // Intercept only if opened FROM settings (see below)
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        db = AppDatabase.getInstance(this)
        pinManager = PinManager(this)
        observeBlockedApps()
    }

    private fun observeBlockedApps() {
        serviceScope.launch {
            db.blockedAppDao().getBlockedApps().collect { apps ->
                val now = System.currentTimeMillis()
                blockedPackages = apps
                    .filter { it.isBlocked }
                    .filter { it.blockedUntil == 0L || it.blockedUntil > now }
                    .map { it.packageName }
                    .toSet()
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        val className   = event.className?.toString()?.lowercase() ?: ""

        // 1. Block app if it's in the parental control list
        if (packageName in blockedPackages && packageName != this.packageName) {
            showBlockScreen(packageName)
            return
        }

        // 2. Protect our own Settings screens from child tampering.
        //    Only intercept if a PIN has already been set (i.e. parental setup is done).
        if (pinManager.isPinSet() && packageName == "com.android.settings") {
            val isSensitive = sensitiveSettingsPatterns.any { pattern ->
                className.contains(pattern, ignoreCase = true)
            }
            if (isSensitive) {
                // Send child home immediately, then require PIN via ChildBlockActivity
                goHome()
                showPinGateForSettings()
                return
            }
        }

        // 3. On Samsung / MIUI devices the settings package name is different
        //    but usually still contains "settings"
        if (pinManager.isPinSet() &&
            (packageName.contains("settings", ignoreCase = true)) &&
            packageName != this.packageName) {
            val isSensitive = sensitiveSettingsPatterns.any { pattern ->
                className.contains(pattern, ignoreCase = true)
            }
            if (isSensitive) {
                goHome()
                showPinGateForSettings()
                return
            }
        }
    }

    private fun showBlockScreen(packageName: String) {
        val intent = Intent(this, ChildBlockActivity::class.java).apply {
            putExtra("blocked_package", packageName)
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
        }
        startActivity(intent)
    }

    private fun showPinGateForSettings() {
        val intent = Intent(this, ChildBlockActivity::class.java).apply {
            putExtra("is_device_admin_disable", true)  // Reuses the PIN gate UI
            putExtra("blocked_package", "com.android.settings")
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
        }
        // Small delay so goHome() completes first
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            startActivity(intent)
        }, 150)
    }

    private fun goHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()  // Prevent coroutine leaks
    }
}
