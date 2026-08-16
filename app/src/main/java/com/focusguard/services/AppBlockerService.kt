package com.focusguard.services

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.focusguard.data.AppDatabase
import com.focusguard.ui.screens.ChildBlockActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class AppBlockerService : Service() {

    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var db: AppDatabase
    private lateinit var limitTracker: DailyLimitTracker
    @Volatile private var blockedPackages = setOf<String>()
    @Volatile private var baseBlockedPackages = setOf<String>()
    @Volatile private var profileBlockedPackages = setOf<String>()
    @Volatile private var limitPackages = mapOf<String, Int>()
    private val handler = Handler(Looper.getMainLooper())

    // Poll every 500 ms — fast enough to block immediately, light enough on battery
    private val checkInterval = 500L

    override fun onCreate() {
        super.onCreate()
        usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        db = AppDatabase.getInstance(this)
        limitTracker = DailyLimitTracker()
        startForeground(NOTIFICATION_ID, buildNotification())
        observeBlockedApps()
        startMonitoring()
    }

    // Managed scope — cancelled in onDestroy to prevent leaks
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Continuously observe the DB. When a timed block expires we auto-clear it
     * so the UI reflects the real state and the block is actually lifted.
     */
    private fun observeBlockedApps() {
        // Run a periodic check to expire timers directly in the database
        serviceScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                db.blockedAppDao().autoExpireApps(now)
                db.blockProfileDao().autoExpireProfiles(now)
                delay(2000L) // Poll every 2 seconds for expirations
            }
        }

        serviceScope.launch {
            db.blockedAppDao().getBlockedApps().collect { apps ->
                val now = System.currentTimeMillis()

                val baseBlocked = apps
                    .filter { it.isBlocked }
                    .filter { it.blockedUntil == 0L || it.blockedUntil > now }
                    .map { it.packageName }
                    .toSet()
                
                baseBlockedPackages = baseBlocked
                blockedPackages = baseBlockedPackages + profileBlockedPackages
                checkAndStopIfIdle()
            }
        }
        serviceScope.launch {
            db.blockProfileDao().getPackagesForActiveProfiles().collect { packages ->
                profileBlockedPackages = packages.toSet()
                blockedPackages = baseBlockedPackages + profileBlockedPackages
                checkAndStopIfIdle()
            }
        }
        serviceScope.launch {
            db.blockProfileDao().getActiveProfiles().collect { activeProfiles ->
                // Flow collected just to keep activeProfiles tracking if we needed it here, 
                // but expiration is now handled natively by the DB timer loop above.
            }
        }
        serviceScope.launch {
            db.usageLimitDao().getAllLimits().collect { limits ->
                limitPackages = limits.associate { it.target to it.limitMinutes }
                checkAndStopIfIdle()
            }
        }
    }

    private fun checkAndStopIfIdle() {
        if (blockedPackages.isEmpty() && limitPackages.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            stopSelf()
        }
    }

    private fun startMonitoring() {
        handler.post(object : Runnable {
            override fun run() {
                checkForegroundApp()
                handler.postDelayed(this, checkInterval)
            }
        })
    }
    private var foregroundApp: String? = null
    private var lastForegroundApp: String? = null
    private fun checkForegroundApp() {
        // Fast-path: Skip expensive UsageStats queries if there's nothing to block!
        if (blockedPackages.isEmpty() && limitPackages.isEmpty()) {
            return
        }

        val now = System.currentTimeMillis()
        // Query the last 10 seconds of raw system window events
        val events = usageStatsManager.queryEvents(now - 10_000L, now)

        val event = UsageEvents.Event()
        
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                foregroundApp = event.packageName
            } else if (event.eventType == UsageEvents.Event.ACTIVITY_PAUSED ||
                       event.eventType == UsageEvents.Event.ACTIVITY_STOPPED) {
                if (foregroundApp == event.packageName) {
                    foregroundApp = null
                }
            }
        }

        val isNewSession = foregroundApp != lastForegroundApp

        val currentApp = foregroundApp
        if (currentApp != null && currentApp != packageName) {
            var shouldBlock = false
            var isDailyLimitBlock = false
            if (currentApp in blockedPackages) {
                shouldBlock = true
            } else if (currentApp in limitPackages) {
                val limitMs = limitPackages[currentApp]!!.times(60_000L)
                val usageTime = limitTracker.checkLimit(
                    packageName = currentApp,
                    isNewSession = isNewSession,
                    pollIntervalMs = checkInterval,
                    todayDayOfYear = getTodaysDay(),
                    fetchBaselineMs = {
                        val startOfToday = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        val stats = usageStatsManager.queryAndAggregateUsageStats(startOfToday, now)
                        stats[currentApp]?.totalTimeInForeground ?: 0L
                    }
                )
                if (usageTime >= limitMs) {
                    shouldBlock = true
                    isDailyLimitBlock = true
                }
            }

            if (shouldBlock) {
                showBlockScreen(currentApp, isDailyLimitBlock)
            }
        }
        lastForegroundApp = foregroundApp
    }
    private fun getTodaysDay(): Int{
        val cal = Calendar.getInstance()
        cal.time = Date()
        return cal.get(Calendar.DAY_OF_YEAR)
    }

    private fun showBlockScreen(packageName: String, isDailyLimitBlock: Boolean = false) {
        val intent = Intent(this, ChildBlockActivity::class.java).apply {
            putExtra("blocked_package", packageName)
            if (isDailyLimitBlock) {
                putExtra("block_reason", "DAILY_LIMIT")
            }
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
        }
        startActivity(intent)
    }

    private fun buildNotification(): Notification {
        val channelId = "focusguard_service"
        val channel = NotificationChannel(
            channelId,
            "FocusGuard Active",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val prefs = getSharedPreferences("FocusGuardPrefs", MODE_PRIVATE)
        val mode = prefs.getString("app_mode", "self_focus")
        val contentText = if (mode == "parental") "Parental controls are running" else "Focus session is active"

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("FocusGuard is active")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.sym_def_app_icon)
            .setOngoing(true)   // can't be swiped away
            .build()
    }

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        serviceScope.cancel()
    }

    // Restart if killed by system
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    companion object {
        const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            context.startForegroundService(Intent(context, AppBlockerService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, AppBlockerService::class.java))
        }
    }
}
