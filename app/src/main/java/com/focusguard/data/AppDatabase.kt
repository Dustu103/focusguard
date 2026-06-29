package com.focusguard.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "blocked_domains")
data class BlockedDomain(
    @PrimaryKey val domain: String
)

@Dao
interface BlockedDomainDao {
    @Query("SELECT * FROM blocked_domains")
    fun getBlockedDomains(): Flow<List<BlockedDomain>>

    @Query("SELECT * FROM blocked_domains")
    suspend fun getAllDomainsNow(): List<BlockedDomain>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDomain(domain: BlockedDomain)

    @Query("DELETE FROM blocked_domains WHERE domain = :domain")
    fun deleteDomain(domain: String)
}

@Database(
    entities = [
        BlockedApp::class, 
        Schedule::class, 
        BlockedDomain::class, 
        UsageLimit::class,
        BlockProfile::class,
        BlockProfileApp::class
    ], 
    version = 4, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun blockedDomainDao(): BlockedDomainDao
    abstract fun usageLimitDao(): UsageLimitDao
    abstract fun blockProfileDao(): BlockProfileDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE schedules ADD COLUMN scheduleType TEXT NOT NULL DEFAULT 'APP'")
                database.execSQL("ALTER TABLE schedules ADD COLUMN targetLabel TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE schedules ADD COLUMN targetUrl TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `usage_limits` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `target` TEXT NOT NULL, `targetType` TEXT NOT NULL, `limitMinutes` INTEGER NOT NULL)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `block_profiles` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `isActive` INTEGER NOT NULL, `activeUntil` INTEGER NOT NULL)")
                database.execSQL("CREATE TABLE IF NOT EXISTS `block_profile_apps` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `profileId` INTEGER NOT NULL, `packageName` TEXT NOT NULL)")
            }
        }

        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "focusguard.db")
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
