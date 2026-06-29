package com.focusguard.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class UsageLimitDao_Impl implements UsageLimitDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<UsageLimit> __insertionAdapterOfUsageLimit;

  private final SharedSQLiteStatement __preparedStmtOfDeleteLimit;

  public UsageLimitDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfUsageLimit = new EntityInsertionAdapter<UsageLimit>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `usage_limits` (`id`,`target`,`targetType`,`limitMinutes`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UsageLimit entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTarget());
        statement.bindString(3, entity.getTargetType());
        statement.bindLong(4, entity.getLimitMinutes());
      }
    };
    this.__preparedStmtOfDeleteLimit = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM usage_limits WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertOrUpdate(final UsageLimit limit,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUsageLimit.insert(limit);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteLimit(final int id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteLimit.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteLimit.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<UsageLimit>> getAllLimits() {
    final String _sql = "SELECT * FROM usage_limits";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"usage_limits"}, new Callable<List<UsageLimit>>() {
      @Override
      @NonNull
      public List<UsageLimit> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTarget = CursorUtil.getColumnIndexOrThrow(_cursor, "target");
          final int _cursorIndexOfTargetType = CursorUtil.getColumnIndexOrThrow(_cursor, "targetType");
          final int _cursorIndexOfLimitMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "limitMinutes");
          final List<UsageLimit> _result = new ArrayList<UsageLimit>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UsageLimit _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTarget;
            _tmpTarget = _cursor.getString(_cursorIndexOfTarget);
            final String _tmpTargetType;
            _tmpTargetType = _cursor.getString(_cursorIndexOfTargetType);
            final int _tmpLimitMinutes;
            _tmpLimitMinutes = _cursor.getInt(_cursorIndexOfLimitMinutes);
            _item = new UsageLimit(_tmpId,_tmpTarget,_tmpTargetType,_tmpLimitMinutes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAllLimitsNow(final Continuation<? super List<UsageLimit>> $completion) {
    final String _sql = "SELECT * FROM usage_limits";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<UsageLimit>>() {
      @Override
      @NonNull
      public List<UsageLimit> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTarget = CursorUtil.getColumnIndexOrThrow(_cursor, "target");
          final int _cursorIndexOfTargetType = CursorUtil.getColumnIndexOrThrow(_cursor, "targetType");
          final int _cursorIndexOfLimitMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "limitMinutes");
          final List<UsageLimit> _result = new ArrayList<UsageLimit>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UsageLimit _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTarget;
            _tmpTarget = _cursor.getString(_cursorIndexOfTarget);
            final String _tmpTargetType;
            _tmpTargetType = _cursor.getString(_cursorIndexOfTargetType);
            final int _tmpLimitMinutes;
            _tmpLimitMinutes = _cursor.getInt(_cursorIndexOfLimitMinutes);
            _item = new UsageLimit(_tmpId,_tmpTarget,_tmpTargetType,_tmpLimitMinutes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
