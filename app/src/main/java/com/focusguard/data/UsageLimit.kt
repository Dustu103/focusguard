package com.focusguard.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "usage_limits")
data class UsageLimit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val target: String,      // e.g. "com.instagram.android" or "Social Media"
    val targetType: String,  // "APP" or "CATEGORY"
    val limitMinutes: Int    // Daily limit in minutes
)

@Dao
interface UsageLimitDao {
    @Query("SELECT * FROM usage_limits")
    fun getAllLimits(): Flow<List<UsageLimit>>

    @Query("SELECT * FROM usage_limits")
    suspend fun getAllLimitsNow(): List<UsageLimit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(limit: UsageLimit)

    @Query("DELETE FROM usage_limits WHERE id = :id")
    suspend fun deleteLimit(id: Int)
}
