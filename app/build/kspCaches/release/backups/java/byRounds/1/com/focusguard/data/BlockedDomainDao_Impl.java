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
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class BlockedDomainDao_Impl implements BlockedDomainDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<BlockedDomain> __insertionAdapterOfBlockedDomain;

  private final SharedSQLiteStatement __preparedStmtOfDeleteDomain;

  public BlockedDomainDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfBlockedDomain = new EntityInsertionAdapter<BlockedDomain>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `blocked_domains` (`domain`) VALUES (?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BlockedDomain entity) {
        statement.bindString(1, entity.getDomain());
      }
    };
    this.__preparedStmtOfDeleteDomain = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM blocked_domains WHERE domain = ?";
        return _query;
      }
    };
  }

  @Override
  public void insertDomain(final BlockedDomain domain) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfBlockedDomain.insert(domain);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteDomain(final String domain) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteDomain.acquire();
    int _argIndex = 1;
    _stmt.bindString(_argIndex, domain);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteDomain.release(_stmt);
    }
  }

  @Override
  public Flow<List<BlockedDomain>> getBlockedDomains() {
    final String _sql = "SELECT * FROM blocked_domains";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"blocked_domains"}, new Callable<List<BlockedDomain>>() {
      @Override
      @NonNull
      public List<BlockedDomain> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDomain = CursorUtil.getColumnIndexOrThrow(_cursor, "domain");
          final List<BlockedDomain> _result = new ArrayList<BlockedDomain>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BlockedDomain _item;
            final String _tmpDomain;
            _tmpDomain = _cursor.getString(_cursorIndexOfDomain);
            _item = new BlockedDomain(_tmpDomain);
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
  public Object getAllDomainsNow(final Continuation<? super List<BlockedDomain>> $completion) {
    final String _sql = "SELECT * FROM blocked_domains";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<BlockedDomain>>() {
      @Override
      @NonNull
      public List<BlockedDomain> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDomain = CursorUtil.getColumnIndexOrThrow(_cursor, "domain");
          final List<BlockedDomain> _result = new ArrayList<BlockedDomain>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BlockedDomain _item;
            final String _tmpDomain;
            _tmpDomain = _cursor.getString(_cursorIndexOfDomain);
            _item = new BlockedDomain(_tmpDomain);
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
