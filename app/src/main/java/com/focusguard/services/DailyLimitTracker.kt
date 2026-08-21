package com.focusguard.services

class DailyLimitTracker {

    private val usageMs = mutableMapOf<String, Long>()
    private val baselineDayOfYear = mutableMapOf<String, Int>()

    fun getUsage(
        packageName: String,
        isNewSession: Boolean,
        pollIntervalMs: Long,
        todayDayOfYear: Int,
        fetchBaselineMs: () -> Long
    ): Long {
        val dayRolledOver = baselineDayOfYear[packageName] != todayDayOfYear

        return if (isNewSession || dayRolledOver) {
            val baseline = fetchBaselineMs()
            usageMs[packageName] = baseline
            baselineDayOfYear[packageName] = todayDayOfYear
            baseline
        } else {
            val updated = (usageMs[packageName] ?: 0L) + pollIntervalMs
            usageMs[packageName] = updated
            updated
        }
    }
}

