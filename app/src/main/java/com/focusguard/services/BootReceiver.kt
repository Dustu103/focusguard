package com.focusguard.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.focusguard.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getInstance(context)
                
                // 1. Restore App Blocker and VPN if there are blocked apps/domains
                val hasActiveAppBlocks = db.blockedAppDao().getBlockedPackageNames().isNotEmpty()
                val hasBlockedDomains = db.blockedDomainDao().getAllDomainsNow().isNotEmpty()
                
                if (hasActiveAppBlocks) {
                    AppBlockerService.start(context)
                }
                
                if (hasBlockedDomains) {
                    // VPN start doesn't need consent on boot if it was already prepared
                    FocusVpnService.start(context)
                }

                // 2. Restore all time-based schedules
                val schedules = db.scheduleDao().getAllSchedulesNow()
                if (schedules.isNotEmpty()) {
                    ScheduleManager(context).scheduleAll(schedules)
                }
            }
        }
    }
}
