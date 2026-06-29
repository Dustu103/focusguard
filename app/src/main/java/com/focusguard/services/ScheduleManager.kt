package com.focusguard.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.focusguard.data.Schedule
import java.util.Calendar

class ScheduleManager(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAll(schedules: List<Schedule>) {
        // Cancel existing first? Hard to do without keeping track of IDs,
        // but we can recreate intents with the exact same ID (schedule.id) to overwrite them.
        schedules.forEach { schedule ->
            if (schedule.isActive) {
                setAlarm(schedule, true)
                setAlarm(schedule, false)
            } else {
                cancelAlarm(schedule, true)
                cancelAlarm(schedule, false)
            }
        }
    }

    fun setAlarm(schedule: Schedule, isStart: Boolean) {
        val intent = Intent(context, ScheduleReceiver::class.java).apply {
            putExtra("action", if (isStart) "START_BLOCK" else "STOP_BLOCK")
            putExtra("scheduleType", schedule.scheduleType)
            putExtra("target", if (schedule.scheduleType == "APP") schedule.packageName else schedule.targetUrl)
        }
        
        // Generate a unique ID for start/stop so they don't overwrite each other
        val requestCode = (schedule.id * 10 + (if (isStart) 1 else 0)).toInt()
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val hour = if (isStart) schedule.startHour else schedule.endHour
        val minute = if (isStart) schedule.startMinute else schedule.endMinute

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            
            // If the time has already passed today, schedule for tomorrow
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        // Repeating alarm for every day
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelAlarm(schedule: Schedule, isStart: Boolean) {
        val intent = Intent(context, ScheduleReceiver::class.java)
        val requestCode = (schedule.id * 10 + (if (isStart) 1 else 0)).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
