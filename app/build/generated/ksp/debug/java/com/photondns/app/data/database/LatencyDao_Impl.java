package com.photondns.app.data.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.photondns.app.data.models.LatencyRecord;
import java.lang.Class;
import java.lang.Double;
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
public final class LatencyDao_Impl implements LatencyDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<LatencyRecord> __insertionAdapterOfLatencyRecord;

  private final EntityDeletionOrUpdateAdapter<LatencyRecord> __deletionAdapterOfLatencyRecord;

  private final SharedSQLiteStatement __preparedStmtOfDeleteLatencyRecordsBefore;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllLatencyRecords;

  public LatencyDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfLatencyRecord = new EntityInsertionAdapter<LatencyRecord>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `latency_records` (`id`,`timestamp`,`dnsServerId`,`dnsServerName`,`dnsServerIp`,`latency`,`success`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LatencyRecord entity) {
        statement.bindString(1, entity.getId());
        statement.bindLong(2, entity.getTimestamp());
        statement.bindString(3, entity.getDnsServerId());
        statement.bindString(4, entity.getDnsServerName());
        statement.bindString(5, entity.getDnsServerIp());
        statement.bindLong(6, entity.getLatency());
        final int _tmp = entity.getSuccess() ? 1 : 0;
        statement.bindLong(7, _tmp);
      }
    };
    this.__deletionAdapterOfLatencyRecord = new EntityDeletionOrUpdateAdapter<LatencyRecord>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `latency_records` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LatencyRecord entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteLatencyRecordsBefore = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM latency_records WHERE timestamp < ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllLatencyRecords = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM latency_records";
        return _query;
      }
    };
  }

  @Override
  public Object insertLatencyRecord(final LatencyRecord record,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfLatencyRecord.insert(record);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertLatencyRecords(final List<LatencyRecord> records,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfLatencyRecord.insert(records);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteLatencyRecord(final LatencyRecord record,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfLatencyRecord.handle(record);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteLatencyRecordsBefore(final long before,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteLatencyRecordsBefore.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, before);
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
          __preparedStmtOfDeleteLatencyRecordsBefore.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllLatencyRecords(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllLatencyRecords.acquire();
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
          __preparedStmtOfDeleteAllLatencyRecords.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<LatencyRecord>> getAllLatencyRecords() {
    final String _sql = "SELECT * FROM latency_records ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"latency_records"}, new Callable<List<LatencyRecord>>() {
      @Override
      @NonNull
      public List<LatencyRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfDnsServerId = CursorUtil.getColumnIndexOrThrow(_cursor, "dnsServerId");
          final int _cursorIndexOfDnsServerName = CursorUtil.getColumnIndexOrThrow(_cursor, "dnsServerName");
          final int _cursorIndexOfDnsServerIp = CursorUtil.getColumnIndexOrThrow(_cursor, "dnsServerIp");
          final int _cursorIndexOfLatency = CursorUtil.getColumnIndexOrThrow(_cursor, "latency");
          final int _cursorIndexOfSuccess = CursorUtil.getColumnIndexOrThrow(_cursor, "success");
          final List<LatencyRecord> _result = new ArrayList<LatencyRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LatencyRecord _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpDnsServerId;
            _tmpDnsServerId = _cursor.getString(_cursorIndexOfDnsServerId);
            final String _tmpDnsServerName;
            _tmpDnsServerName = _cursor.getString(_cursorIndexOfDnsServerName);
            final String _tmpDnsServerIp;
            _tmpDnsServerIp = _cursor.getString(_cursorIndexOfDnsServerIp);
            final int _tmpLatency;
            _tmpLatency = _cursor.getInt(_cursorIndexOfLatency);
            final boolean _tmpSuccess;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSuccess);
            _tmpSuccess = _tmp != 0;
            _item = new LatencyRecord(_tmpId,_tmpTimestamp,_tmpDnsServerId,_tmpDnsServerName,_tmpDnsServerIp,_tmpLatency,_tmpSuccess);
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
  public Object getLatencyRecordsForServer(final String serverId, final int limit,
      final Continuation<? super List<LatencyRecord>> $completion) {
    final String _sql = "SELECT * FROM latency_records WHERE dnsServerId = ? ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, serverId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<LatencyRecord>>() {
      @Override
      @NonNull
      public List<LatencyRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfDnsServerId = CursorUtil.getColumnIndexOrThrow(_cursor, "dnsServerId");
          final int _cursorIndexOfDnsServerName = CursorUtil.getColumnIndexOrThrow(_cursor, "dnsServerName");
          final int _cursorIndexOfDnsServerIp = CursorUtil.getColumnIndexOrThrow(_cursor, "dnsServerIp");
          final int _cursorIndexOfLatency = CursorUtil.getColumnIndexOrThrow(_cursor, "latency");
          final int _cursorIndexOfSuccess = CursorUtil.getColumnIndexOrThrow(_cursor, "success");
          final List<LatencyRecord> _result = new ArrayList<LatencyRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LatencyRecord _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpDnsServerId;
            _tmpDnsServerId = _cursor.getString(_cursorIndexOfDnsServerId);
            final String _tmpDnsServerName;
            _tmpDnsServerName = _cursor.getString(_cursorIndexOfDnsServerName);
            final String _tmpDnsServerIp;
            _tmpDnsServerIp = _cursor.getString(_cursorIndexOfDnsServerIp);
            final int _tmpLatency;
            _tmpLatency = _cursor.getInt(_cursorIndexOfLatency);
            final boolean _tmpSuccess;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSuccess);
            _tmpSuccess = _tmp != 0;
            _item = new LatencyRecord(_tmpId,_tmpTimestamp,_tmpDnsServerId,_tmpDnsServerName,_tmpDnsServerIp,_tmpLatency,_tmpSuccess);
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

  @Override
  public Object getLatencyRecordsSince(final long since,
      final Continuation<? super List<LatencyRecord>> $completion) {
    final String _sql = "SELECT * FROM latency_records WHERE timestamp >= ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, since);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<LatencyRecord>>() {
      @Override
      @NonNull
      public List<LatencyRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfDnsServerId = CursorUtil.getColumnIndexOrThrow(_cursor, "dnsServerId");
          final int _cursorIndexOfDnsServerName = CursorUtil.getColumnIndexOrThrow(_cursor, "dnsServerName");
          final int _cursorIndexOfDnsServerIp = CursorUtil.getColumnIndexOrThrow(_cursor, "dnsServerIp");
          final int _cursorIndexOfLatency = CursorUtil.getColumnIndexOrThrow(_cursor, "latency");
          final int _cursorIndexOfSuccess = CursorUtil.getColumnIndexOrThrow(_cursor, "success");
          final List<LatencyRecord> _result = new ArrayList<LatencyRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LatencyRecord _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpDnsServerId;
            _tmpDnsServerId = _cursor.getString(_cursorIndexOfDnsServerId);
            final String _tmpDnsServerName;
            _tmpDnsServerName = _cursor.getString(_cursorIndexOfDnsServerName);
            final String _tmpDnsServerIp;
            _tmpDnsServerIp = _cursor.getString(_cursorIndexOfDnsServerIp);
            final int _tmpLatency;
            _tmpLatency = _cursor.getInt(_cursorIndexOfLatency);
            final boolean _tmpSuccess;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSuccess);
            _tmpSuccess = _tmp != 0;
            _item = new LatencyRecord(_tmpId,_tmpTimestamp,_tmpDnsServerId,_tmpDnsServerName,_tmpDnsServerIp,_tmpLatency,_tmpSuccess);
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

  @Override
  public Object getLatencyRecordsForServerSince(final String serverId, final long since,
      final Continuation<? super List<LatencyRecord>> $completion) {
    final String _sql = "SELECT * FROM latency_records WHERE dnsServerId = ? AND timestamp >= ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, serverId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, since);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<LatencyRecord>>() {
      @Override
      @NonNull
      public List<LatencyRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfDnsServerId = CursorUtil.getColumnIndexOrThrow(_cursor, "dnsServerId");
          final int _cursorIndexOfDnsServerName = CursorUtil.getColumnIndexOrThrow(_cursor, "dnsServerName");
          final int _cursorIndexOfDnsServerIp = CursorUtil.getColumnIndexOrThrow(_cursor, "dnsServerIp");
          final int _cursorIndexOfLatency = CursorUtil.getColumnIndexOrThrow(_cursor, "latency");
          final int _cursorIndexOfSuccess = CursorUtil.getColumnIndexOrThrow(_cursor, "success");
          final List<LatencyRecord> _result = new ArrayList<LatencyRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LatencyRecord _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpDnsServerId;
            _tmpDnsServerId = _cursor.getString(_cursorIndexOfDnsServerId);
            final String _tmpDnsServerName;
            _tmpDnsServerName = _cursor.getString(_cursorIndexOfDnsServerName);
            final String _tmpDnsServerIp;
            _tmpDnsServerIp = _cursor.getString(_cursorIndexOfDnsServerIp);
            final int _tmpLatency;
            _tmpLatency = _cursor.getInt(_cursorIndexOfLatency);
            final boolean _tmpSuccess;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSuccess);
            _tmpSuccess = _tmp != 0;
            _item = new LatencyRecord(_tmpId,_tmpTimestamp,_tmpDnsServerId,_tmpDnsServerName,_tmpDnsServerIp,_tmpLatency,_tmpSuccess);
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

  @Override
  public Object getAverageLatencyForServer(final String serverId, final long since,
      final Continuation<? super Double> $completion) {
    final String _sql = "SELECT AVG(latency) as avgLatency FROM latency_records WHERE dnsServerId = ? AND success = 1 AND timestamp >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, serverId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, since);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Double>() {
      @Override
      @Nullable
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getLatestLatencyRecord(final Continuation<? super LatencyRecord> $completion) {
    final String _sql = "SELECT * FROM latency_records ORDER BY timestamp DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<LatencyRecord>() {
      @Override
      @Nullable
      public LatencyRecord call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfDnsServerId = CursorUtil.getColumnIndexOrThrow(_cursor, "dnsServerId");
          final int _cursorIndexOfDnsServerName = CursorUtil.getColumnIndexOrThrow(_cursor, "dnsServerName");
          final int _cursorIndexOfDnsServerIp = CursorUtil.getColumnIndexOrThrow(_cursor, "dnsServerIp");
          final int _cursorIndexOfLatency = CursorUtil.getColumnIndexOrThrow(_cursor, "latency");
          final int _cursorIndexOfSuccess = CursorUtil.getColumnIndexOrThrow(_cursor, "success");
          final LatencyRecord _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpDnsServerId;
            _tmpDnsServerId = _cursor.getString(_cursorIndexOfDnsServerId);
            final String _tmpDnsServerName;
            _tmpDnsServerName = _cursor.getString(_cursorIndexOfDnsServerName);
            final String _tmpDnsServerIp;
            _tmpDnsServerIp = _cursor.getString(_cursorIndexOfDnsServerIp);
            final int _tmpLatency;
            _tmpLatency = _cursor.getInt(_cursorIndexOfLatency);
            final boolean _tmpSuccess;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSuccess);
            _tmpSuccess = _tmp != 0;
            _result = new LatencyRecord(_tmpId,_tmpTimestamp,_tmpDnsServerId,_tmpDnsServerName,_tmpDnsServerIp,_tmpLatency,_tmpSuccess);
          } else {
            _result = null;
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
