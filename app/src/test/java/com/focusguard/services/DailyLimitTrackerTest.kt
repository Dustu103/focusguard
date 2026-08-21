package com.focusguard.services

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyLimitTrackerTest {

    @Test
    fun `new session fetches baseline from UsageStatsManager`() {
        val tracker = DailyLimitTracker()

        val used = tracker.getUsage(
            packageName = "com.example.game",
            isNewSession = true,
            pollIntervalMs = 500L,
            todayDayOfYear = 1,
            fetchBaselineMs = { 55 * 60_000L } // pretend UsageStatsManager says 55 min so far today
        )

        assertEquals(55 * 60_000L, used)
    }

    @Test
    fun `same session adds poll interval without refetching baseline`() {
        val tracker = DailyLimitTracker()
        var baselineCalls = 0
        val fetchBaseline = { baselineCalls++; 0L }

        tracker.getUsage("com.example.game", isNewSession = true, pollIntervalMs = 500L, todayDayOfYear = 1, fetchBaselineMs = fetchBaseline)
        val used = tracker.getUsage("com.example.game", isNewSession = false, pollIntervalMs = 500L, todayDayOfYear = 1, fetchBaselineMs = fetchBaseline)

        // baseline (0) from the first tick, then +500ms from the second tick
        assertEquals(500L, used)
        assertEquals(1, baselineCalls) // UsageStatsManager only queried once for the whole session
    }

    @Test
    fun `crosses the limit after enough ticks`() {
        val tracker = DailyLimitTracker()
        val limitMs = 60 * 60_000L // 60-minute limit

        var used = tracker.getUsage(
            "com.example.game",
            isNewSession = true,
            pollIntervalMs = 500L,
            todayDayOfYear = 1,
            fetchBaselineMs = { 59 * 60_000L + 59_500L } // 59:59.5 baseline
        )
        assertTrue(used < limitMs)

        used = tracker.getUsage("com.example.game", isNewSession = false, pollIntervalMs = 500L, todayDayOfYear = 1, fetchBaselineMs = { 0L })
        assertEquals(60 * 60_000L, used)
        assertTrue(used >= limitMs)
    }

    @Test
    fun `midnight rollover forces a fresh baseline even mid-session`() {
        val tracker = DailyLimitTracker()
        var baselineCalls = 0

        tracker.getUsage("com.example.game", isNewSession = true, pollIntervalMs = 500L, todayDayOfYear = 100, fetchBaselineMs = { baselineCalls++; 59 * 60_000L })
        // Same continuous session, but the day rolled over (app stayed foregrounded across midnight)
        val used = tracker.getUsage("com.example.game", isNewSession = false, pollIntervalMs = 500L, todayDayOfYear = 101, fetchBaselineMs = { baselineCalls++; 0L })

        assertEquals(2, baselineCalls)
        assertEquals(0L, used) // yesterday's usage must not carry into today's limit
    }

    @Test
    fun `different packages are tracked independently`() {
        val tracker = DailyLimitTracker()

        tracker.getUsage("pkg.a", isNewSession = true, pollIntervalMs = 500L, todayDayOfYear = 1, fetchBaselineMs = { 10_000L })
        val usedB = tracker.getUsage("pkg.b", isNewSession = true, pollIntervalMs = 500L, todayDayOfYear = 1, fetchBaselineMs = { 20_000L })

        assertEquals(20_000L, usedB)
    }
}
