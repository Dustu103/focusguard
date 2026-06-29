package com.focusguard.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile BlockedAppDao _blockedAppDao;

  private volatile ScheduleDao _scheduleDao;

  private volatile BlockedDomainDao _blockedDomainDao;

  private volatile UsageLimitDao _usageLimitDao;

  private volatile BlockProfileDao _blockProfileDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(4) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `blocked_apps` (`packageName` TEXT NOT NULL, `appName` TEXT NOT NULL, `appIcon` BLOB, `isBlocked` INTEGER NOT NULL, `blockedUntil` INTEGER NOT NULL, `useSchedule` INTEGER NOT NULL, PRIMARY KEY(`packageName`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `schedules` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `scheduleType` TEXT NOT NULL, `packageName` TEXT NOT NULL, `targetLabel` TEXT NOT NULL, `targetUrl` TEXT NOT NULL, `startHour` INTEGER NOT NULL, `startMinute` INTEGER NOT NULL, `endHour` INTEGER NOT NULL, `endMinute` INTEGER NOT NULL, `daysOfWeek` TEXT NOT NULL, `isActive` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `blocked_domains` (`domain` TEXT NOT NULL, PRIMARY KEY(`domain`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `usage_limits` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `target` TEXT NOT NULL, `targetType` TEXT NOT NULL, `limitMinutes` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `block_profiles` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `isActive` INTEGER NOT NULL, `activeUntil` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `block_profile_apps` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `profileId` INTEGER NOT NULL, `packageName` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'c166301acec80d06606b46b8d9a10684')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `blocked_apps`");
        db.execSQL("DROP TABLE IF EXISTS `schedules`");
        db.execSQL("DROP TABLE IF EXISTS `blocked_domains`");
        db.execSQL("DROP TABLE IF EXISTS `usage_limits`");
        db.execSQL("DROP TABLE IF EXISTS `block_profiles`");
        db.execSQL("DROP TABLE IF EXISTS `block_profile_apps`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsBlockedApps = new HashMap<String, TableInfo.Column>(6);
        _columnsBlockedApps.put("packageName", new TableInfo.Column("packageName", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBlockedApps.put("appName", new TableInfo.Column("appName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBlockedApps.put("appIcon", new TableInfo.Column("appIcon", "BLOB", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBlockedApps.put("isBlocked", new TableInfo.Column("isBlocked", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBlockedApps.put("blockedUntil", new TableInfo.Column("blockedUntil", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBlockedApps.put("useSchedule", new TableInfo.Column("useSchedule", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBlockedApps = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesBlockedApps = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoBlockedApps = new TableInfo("blocked_apps", _columnsBlockedApps, _foreignKeysBlockedApps, _indicesBlockedApps);
        final TableInfo _existingBlockedApps = TableInfo.read(db, "blocked_apps");
        if (!_infoBlockedApps.equals(_existingBlockedApps)) {
          return new RoomOpenHelper.ValidationResult(false, "blocked_apps(com.focusguard.data.BlockedApp).\n"
                  + " Expected:\n" + _infoBlockedApps + "\n"
                  + " Found:\n" + _existingBlockedApps);
        }
        final HashMap<String, TableInfo.Column> _columnsSchedules = new HashMap<String, TableInfo.Column>(11);
        _columnsSchedules.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("scheduleType", new TableInfo.Column("scheduleType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("packageName", new TableInfo.Column("packageName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("targetLabel", new TableInfo.Column("targetLabel", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("targetUrl", new TableInfo.Column("targetUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("startHour", new TableInfo.Column("startHour", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("startMinute", new TableInfo.Column("startMinute", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("endHour", new TableInfo.Column("endHour", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("endMinute", new TableInfo.Column("endMinute", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("daysOfWeek", new TableInfo.Column("daysOfWeek", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSchedules = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSchedules = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSchedules = new TableInfo("schedules", _columnsSchedules, _foreignKeysSchedules, _indicesSchedules);
        final TableInfo _existingSchedules = TableInfo.read(db, "schedules");
        if (!_infoSchedules.equals(_existingSchedules)) {
          return new RoomOpenHelper.ValidationResult(false, "schedules(com.focusguard.data.Schedule).\n"
                  + " Expected:\n" + _infoSchedules + "\n"
                  + " Found:\n" + _existingSchedules);
        }
        final HashMap<String, TableInfo.Column> _columnsBlockedDomains = new HashMap<String, TableInfo.Column>(1);
        _columnsBlockedDomains.put("domain", new TableInfo.Column("domain", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBlockedDomains = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesBlockedDomains = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoBlockedDomains = new TableInfo("blocked_domains", _columnsBlockedDomains, _foreignKeysBlockedDomains, _indicesBlockedDomains);
        final TableInfo _existingBlockedDomains = TableInfo.read(db, "blocked_domains");
        if (!_infoBlockedDomains.equals(_existingBlockedDomains)) {
          return new RoomOpenHelper.ValidationResult(false, "blocked_domains(com.focusguard.data.BlockedDomain).\n"
                  + " Expected:\n" + _infoBlockedDomains + "\n"
                  + " Found:\n" + _existingBlockedDomains);
        }
        final HashMap<String, TableInfo.Column> _columnsUsageLimits = new HashMap<String, TableInfo.Column>(4);
        _columnsUsageLimits.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsageLimits.put("target", new TableInfo.Column("target", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsageLimits.put("targetType", new TableInfo.Column("targetType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsageLimits.put("limitMinutes", new TableInfo.Column("limitMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUsageLimits = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUsageLimits = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUsageLimits = new TableInfo("usage_limits", _columnsUsageLimits, _foreignKeysUsageLimits, _indicesUsageLimits);
        final TableInfo _existingUsageLimits = TableInfo.read(db, "usage_limits");
        if (!_infoUsageLimits.equals(_existingUsageLimits)) {
          return new RoomOpenHelper.ValidationResult(false, "usage_limits(com.focusguard.data.UsageLimit).\n"
                  + " Expected:\n" + _infoUsageLimits + "\n"
                  + " Found:\n" + _existingUsageLimits);
        }
        final HashMap<String, TableInfo.Column> _columnsBlockProfiles = new HashMap<String, TableInfo.Column>(4);
        _columnsBlockProfiles.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBlockProfiles.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBlockProfiles.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBlockProfiles.put("activeUntil", new TableInfo.Column("activeUntil", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBlockProfiles = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesBlockProfiles = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoBlockProfiles = new TableInfo("block_profiles", _columnsBlockProfiles, _foreignKeysBlockProfiles, _indicesBlockProfiles);
        final TableInfo _existingBlockProfiles = TableInfo.read(db, "block_profiles");
        if (!_infoBlockProfiles.equals(_existingBlockProfiles)) {
          return new RoomOpenHelper.ValidationResult(false, "block_profiles(com.focusguard.data.BlockProfile).\n"
                  + " Expected:\n" + _infoBlockProfiles + "\n"
                  + " Found:\n" + _existingBlockProfiles);
        }
        final HashMap<String, TableInfo.Column> _columnsBlockProfileApps = new HashMap<String, TableInfo.Column>(3);
        _columnsBlockProfileApps.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBlockProfileApps.put("profileId", new TableInfo.Column("profileId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBlockProfileApps.put("packageName", new TableInfo.Column("packageName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBlockProfileApps = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesBlockProfileApps = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoBlockProfileApps = new TableInfo("block_profile_apps", _columnsBlockProfileApps, _foreignKeysBlockProfileApps, _indicesBlockProfileApps);
        final TableInfo _existingBlockProfileApps = TableInfo.read(db, "block_profile_apps");
        if (!_infoBlockProfileApps.equals(_existingBlockProfileApps)) {
          return new RoomOpenHelper.ValidationResult(false, "block_profile_apps(com.focusguard.data.BlockProfileApp).\n"
                  + " Expected:\n" + _infoBlockProfileApps + "\n"
                  + " Found:\n" + _existingBlockProfileApps);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "c166301acec80d06606b46b8d9a10684", "2d082f5a429f9340d2983821f71914f7");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "blocked_apps","schedules","blocked_domains","usage_limits","block_profiles","block_profile_apps");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `blocked_apps`");
      _db.execSQL("DELETE FROM `schedules`");
      _db.execSQL("DELETE FROM `blocked_domains`");
      _db.execSQL("DELETE FROM `usage_limits`");
      _db.execSQL("DELETE FROM `block_profiles`");
      _db.execSQL("DELETE FROM `block_profile_apps`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(BlockedAppDao.class, BlockedAppDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ScheduleDao.class, ScheduleDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(BlockedDomainDao.class, BlockedDomainDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(UsageLimitDao.class, UsageLimitDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(BlockProfileDao.class, BlockProfileDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public BlockedAppDao blockedAppDao() {
    if (_blockedAppDao != null) {
      return _blockedAppDao;
    } else {
      synchronized(this) {
        if(_blockedAppDao == null) {
          _blockedAppDao = new BlockedAppDao_Impl(this);
        }
        return _blockedAppDao;
      }
    }
  }

  @Override
  public ScheduleDao scheduleDao() {
    if (_scheduleDao != null) {
      return _scheduleDao;
    } else {
      synchronized(this) {
        if(_scheduleDao == null) {
          _scheduleDao = new ScheduleDao_Impl(this);
        }
        return _scheduleDao;
      }
    }
  }

  @Override
  public BlockedDomainDao blockedDomainDao() {
    if (_blockedDomainDao != null) {
      return _blockedDomainDao;
    } else {
      synchronized(this) {
        if(_blockedDomainDao == null) {
          _blockedDomainDao = new BlockedDomainDao_Impl(this);
        }
        return _blockedDomainDao;
      }
    }
  }

  @Override
  public UsageLimitDao usageLimitDao() {
    if (_usageLimitDao != null) {
      return _usageLimitDao;
    } else {
      synchronized(this) {
        if(_usageLimitDao == null) {
          _usageLimitDao = new UsageLimitDao_Impl(this);
        }
        return _usageLimitDao;
      }
    }
  }

  @Override
  public BlockProfileDao blockProfileDao() {
    if (_blockProfileDao != null) {
      return _blockProfileDao;
    } else {
      synchronized(this) {
        if(_blockProfileDao == null) {
          _blockProfileDao = new BlockProfileDao_Impl(this);
        }
        return _blockProfileDao;
      }
    }
  }
}
