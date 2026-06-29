package com.focusguard.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_apps")
data class BlockedApp(
    @PrimaryKey val packageName: String,
    val appName: String,
    val appIcon: ByteArray? = null,
    val isBlocked: Boolean = true,
    val blockedUntil: Long = 0L,
    val useSchedule: Boolean = false
)

@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    // "APP" or "URL"
    val scheduleType: String = "APP",
    // used when scheduleType == "APP"
    val packageName: String = "",
    // friendly display name for the target (app name or domain label)
    val targetLabel: String = "",
    // used when scheduleType == "URL"
    val targetUrl: String = "",
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val daysOfWeek: String,
    val isActive: Boolean = true
)
