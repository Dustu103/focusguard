package com.focusguard.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.focusguard.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getStringExtra("action") ?: return
        val scheduleType = intent.getStringExtra("scheduleType") ?: "APP"
        val target = intent.getStringExtra("target") ?: ""

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(context)
            when (action) {
                "START_BLOCK" -> {
                    if (scheduleType == "APP") {
                        if (target.isEmpty() || target == "ALL") {
                            db.blockedAppDao().setAllScheduledBlocked(true)
                        } else {
                            db.blockedAppDao().setBlocked(target, true)
                        }
                        AppBlockerService.start(context)
                    } else if (scheduleType == "URL" && target.isNotEmpty()) {
                        db.blockedDomainDao().insertDomain(com.focusguard.data.BlockedDomain(target))
                        FocusVpnService.start(context)
                    }
                }
                "STOP_BLOCK" -> {
                    if (scheduleType == "APP") {
                        if (target.isEmpty() || target == "ALL") {
                            db.blockedAppDao().setAllScheduledBlocked(false)
                        } else {
                            db.blockedAppDao().setBlocked(target, false)
                        }
                    } else if (scheduleType == "URL" && target.isNotEmpty()) {
                        db.blockedDomainDao().deleteDomain(target)
                        FocusVpnService.start(context) // Restart VPN to apply deletion
                    }
                }
            }
        }
    }
}
