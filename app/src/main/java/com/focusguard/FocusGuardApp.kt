package com.focusguard

import android.app.Application
import com.focusguard.services.AppBlockerService

class FocusGuardApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Always start the blocker service when the app process starts.
        // The service is a foreground service so Android keeps it alive.
        try {
            AppBlockerService.start(this)
        } catch (e: Exception) {
            android.util.Log.e("FocusGuardApp", "Could not start service from background", e)
        }
    }
}
