package com.focusguard.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedAppDao {
    // Returns ALL apps (not just blocked) so the Flow emits when isBlocked flips
    // from 1→0 on timer expiry, allowing services to clear their domain cache.
    @Query("SELECT * FROM blocked_apps")
    fun getBlockedApps(): Flow<List<BlockedApp>>

    @Query("SELECT * FROM blocked_apps")
    suspend fun getAllApps(): List<BlockedApp>

    @Query("SELECT packageName FROM blocked_apps WHERE isBlocked = 1")
    suspend fun getBlockedPackageNames(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(app: BlockedApp)

    @Query("UPDATE blocked_apps SET isBlocked = :blocked WHERE packageName = :packageName")
    suspend fun setBlocked(packageName: String, blocked: Boolean)

    @Query("UPDATE blocked_apps SET blockedUntil = :until WHERE packageName = :packageName")
    suspend fun setBlockedUntil(packageName: String, until: Long)

    @Query("DELETE FROM blocked_apps WHERE packageName = :packageName")
    suspend fun delete(packageName: String)
    
    @Query("UPDATE blocked_apps SET isBlocked = :blocked WHERE useSchedule = 1")
    suspend fun setAllScheduledBlocked(blocked: Boolean)
    
    @Query("UPDATE blocked_apps SET isBlocked = 0, blockedUntil = 0 WHERE isBlocked = 1 AND blockedUntil > 0 AND blockedUntil <= :currentTime")
    suspend fun autoExpireApps(currentTime: Long)
}
