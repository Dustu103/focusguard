package com.focusguard.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "block_profiles")
data class BlockProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isActive: Boolean = false,
    val activeUntil: Long = 0L // 0 means active forever
)

@Entity(tableName = "block_profile_apps")
data class BlockProfileApp(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: Int,
    val packageName: String
)

data class ProfileWithApps(
    val profile: BlockProfile,
    val apps: List<BlockProfileApp>
)

@Dao
interface BlockProfileDao {
    @Query("SELECT * FROM block_profiles")
    fun getAllProfiles(): Flow<List<BlockProfile>>

    @Query("SELECT * FROM block_profiles WHERE isActive = 1")
    fun getActiveProfiles(): Flow<List<BlockProfile>>

    @Query("SELECT * FROM block_profiles WHERE id = :profileId")
    suspend fun getProfile(profileId: Int): BlockProfile?

    @Query("SELECT * FROM block_profile_apps WHERE profileId = :profileId")
    fun getAppsForProfile(profileId: Int): Flow<List<BlockProfileApp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: BlockProfile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfileApp(app: BlockProfileApp)

    @Query("DELETE FROM block_profile_apps WHERE profileId = :profileId AND packageName = :packageName")
    suspend fun deleteProfileApp(profileId: Int, packageName: String)

    @Query("DELETE FROM block_profiles WHERE id = :profileId")
    suspend fun deleteProfile(profileId: Int)

    @Query("DELETE FROM block_profile_apps WHERE profileId = :profileId")
    suspend fun deleteAppsForProfile(profileId: Int)
    
    @Query("UPDATE block_profiles SET isActive = :isActive, activeUntil = :activeUntil WHERE id = :profileId")
    suspend fun updateProfileStatus(profileId: Int, isActive: Boolean, activeUntil: Long)
    @Query("SELECT DISTINCT p.packageName FROM block_profile_apps p INNER JOIN block_profiles b ON p.profileId = b.id WHERE b.isActive = 1")
    fun getPackagesForActiveProfiles(): Flow<List<String>>
}
