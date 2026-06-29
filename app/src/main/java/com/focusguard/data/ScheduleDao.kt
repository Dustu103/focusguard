package com.focusguard.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules ORDER BY startHour, startMinute")
    fun getAllSchedules(): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules ORDER BY startHour, startMinute")
    suspend fun getAllSchedulesNow(): List<Schedule>

    @Query("SELECT * FROM schedules WHERE isActive = 1")
    suspend fun getActiveSchedules(): List<Schedule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(schedule: Schedule)

    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun delete(id: Int)
}
