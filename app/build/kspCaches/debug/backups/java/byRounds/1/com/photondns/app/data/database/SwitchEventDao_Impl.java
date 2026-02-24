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
import com.photondns.app.data.models.DNSSwitchEvent;
import com.photondns.app.data.models.SwitchReason;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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
public final class SwitchEventDao_Impl implements SwitchEventDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DNSSwitchEvent> __insertionAdapterOfDNSSwitchEvent;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<DNSSwitchEvent> __deletionAdapterOfDNSSwitchEvent;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSwitchEventsBefore;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllSwitchEvents;

  public SwitchEventDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDNSSwitchEvent = new EntityInsertionAdapter<DNSSwitchEvent>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `dns_switch_events` (`id`,`timestamp`,`fromDnsServerId`,`fromDnsServerName`,`toDnsServerId`,`toDnsServerName`,`reason`,`previousLatency`,`newLatency`,`improvement`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DNSSwitchEvent entity) {
        statement.bindString(1, entity.getId());
        statement.bindLong(2, entity.getTimestamp());
        statement.bindString(3, entity.getFromDnsServerId());
        statement.bindString(4, entity.getFromDnsServerName());
        statement.bindString(5, entity.getToDnsServerId());
        statement.bindString(6, entity.getToDnsServerName());
        final String _tmp = __converters.fromSwitchReason(entity.getReason());
        statement.bindString(7, _tmp);
        statement.bindLong(8, entity.getPreviousLatency());
        statement.bindLong(9, entity.getNewLatency());
        statement.bindLong(10, entity.getImprovement());
      }
    };
    this.__deletionAdapterOfDNSSwitchEvent = new EntityDeletionOrUpdateAdapter<DNSSwitchEvent>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `dns_switch_events` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DNSSwitchEvent entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteSwitchEventsBefore = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM dns_switch_events WHERE timestamp < ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllSwitchEvents = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM dns_switch_events";
        return _query;
      }
    };
  }

  @Override
  public Object insertSwitchEvent(final DNSSwitchEvent event,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDNSSwitchEvent.insert(event);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSwitchEvent(final DNSSwitchEvent event,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfDNSSwitchEvent.handle(event);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSwitchEventsBefore(final long before,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSwitchEventsBefore.acquire();
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
          __preparedStmtOfDeleteSwitchEventsBefore.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllSwitchEvents(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllSwitchEvents.acquire();
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
          __preparedStmtOfDeleteAllSwitchEvents.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<DNSSwitchEvent>> getAllSwitchEvents() {
    final String _sql = "SELECT * FROM dns_switch_events ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"dns_switch_events"}, new Callable<List<DNSSwitchEvent>>() {
      @Override
      @NonNull
      public List<DNSSwitchEvent> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfFromDnsServerId = CursorUtil.getColumnIndexOrThrow(_cursor, "fromDnsServerId");
          final int _cursorIndexOfFromDnsServerName = CursorUtil.getColumnIndexOrThrow(_cursor, "fromDnsServerName");
          final int _cursorIndexOfToDnsServerId = CursorUtil.getColumnIndexOrThrow(_cursor, "toDnsServerId");
          final int _cursorIndexOfToDnsServerName = CursorUtil.getColumnIndexOrThrow(_cursor, "toDnsServerName");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfPreviousLatency = CursorUtil.getColumnIndexOrThrow(_cursor, "previousLatency");
          final int _cursorIndexOfNewLatency = CursorUtil.getColumnIndexOrThrow(_cursor, "newLatency");
          final int _cursorIndexOfImprovement = CursorUtil.getColumnIndexOrThrow(_cursor, "improvement");
          final List<DNSSwitchEvent> _result = new ArrayList<DNSSwitchEvent>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DNSSwitchEvent _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpFromDnsServerId;
            _tmpFromDnsServerId = _cursor.getString(_cursorIndexOfFromDnsServerId);
            final String _tmpFromDnsServerName;
            _tmpFromDnsServerName = _cursor.getString(_cursorIndexOfFromDnsServerName);
            final String _tmpToDnsServerId;
            _tmpToDnsServerId = _cursor.getString(_cursorIndexOfToDnsServerId);
            final String _tmpToDnsServerName;
            _tmpToDnsServerName = _cursor.getString(_cursorIndexOfToDnsServerName);
            final SwitchReason _tmpReason;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfReason);
            _tmpReason = __converters.toSwitchReason(_tmp);
            final int _tmpPreviousLatency;
            _tmpPreviousLatency = _cursor.getInt(_cursorIndexOfPreviousLatency);
            final int _tmpNewLatency;
            _tmpNewLatency = _cursor.getInt(_cursorIndexOfNewLatency);
            final int _tmpImprovement;
            _tmpImprovement = _cursor.getInt(_cursorIndexOfImprovement);
            _item = new DNSSwitchEvent(_tmpId,_tmpTimestamp,_tmpFromDnsServerId,_tmpFromDnsServerName,_tmpToDnsServerId,_tmpToDnsServerName,_tmpReason,_tmpPreviousLatency,_tmpNewLatency,_tmpImprovement);
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
  public Object getRecentSwitchEvents(final int limit,
      final Continuation<? super List<DNSSwitchEvent>> $completion) {
    final String _sql = "SELECT * FROM dns_switch_events ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DNSSwitchEvent>>() {
      @Override
      @NonNull
      public List<DNSSwitchEvent> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfFromDnsServerId = CursorUtil.getColumnIndexOrThrow(_cursor, "fromDnsServerId");
          final int _cursorIndexOfFromDnsServerName = CursorUtil.getColumnIndexOrThrow(_cursor, "fromDnsServerName");
          final int _cursorIndexOfToDnsServerId = CursorUtil.getColumnIndexOrThrow(_cursor, "toDnsServerId");
          final int _cursorIndexOfToDnsServerName = CursorUtil.getColumnIndexOrThrow(_cursor, "toDnsServerName");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfPreviousLatency = CursorUtil.getColumnIndexOrThrow(_cursor, "previousLatency");
          final int _cursorIndexOfNewLatency = CursorUtil.getColumnIndexOrThrow(_cursor, "newLatency");
          final int _cursorIndexOfImprovement = CursorUtil.getColumnIndexOrThrow(_cursor, "improvement");
          final List<DNSSwitchEvent> _result = new ArrayList<DNSSwitchEvent>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DNSSwitchEvent _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpFromDnsServerId;
            _tmpFromDnsServerId = _cursor.getString(_cursorIndexOfFromDnsServerId);
            final String _tmpFromDnsServerName;
            _tmpFromDnsServerName = _cursor.getString(_cursorIndexOfFromDnsServerName);
            final String _tmpToDnsServerId;
            _tmpToDnsServerId = _cursor.getString(_cursorIndexOfToDnsServerId);
            final String _tmpToDnsServerName;
            _tmpToDnsServerName = _cursor.getString(_cursorIndexOfToDnsServerName);
            final SwitchReason _tmpReason;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfReason);
            _tmpReason = __converters.toSwitchReason(_tmp);
            final int _tmpPreviousLatency;
            _tmpPreviousLatency = _cursor.getInt(_cursorIndexOfPreviousLatency);
            final int _tmpNewLatency;
            _tmpNewLatency = _cursor.getInt(_cursorIndexOfNewLatency);
            final int _tmpImprovement;
            _tmpImprovement = _cursor.getInt(_cursorIndexOfImprovement);
            _item = new DNSSwitchEvent(_tmpId,_tmpTimestamp,_tmpFromDnsServerId,_tmpFromDnsServerName,_tmpToDnsServerId,_tmpToDnsServerName,_tmpReason,_tmpPreviousLatency,_tmpNewLatency,_tmpImprovement);
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
  public Object getSwitchEventsSince(final long since,
      final Continuation<? super List<DNSSwitchEvent>> $completion) {
    final String _sql = "SELECT * FROM dns_switch_events WHERE timestamp >= ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, since);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DNSSwitchEvent>>() {
      @Override
      @NonNull
      public List<DNSSwitchEvent> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfFromDnsServerId = CursorUtil.getColumnIndexOrThrow(_cursor, "fromDnsServerId");
          final int _cursorIndexOfFromDnsServerName = CursorUtil.getColumnIndexOrThrow(_cursor, "fromDnsServerName");
          final int _cursorIndexOfToDnsServerId = CursorUtil.getColumnIndexOrThrow(_cursor, "toDnsServerId");
          final int _cursorIndexOfToDnsServerName = CursorUtil.getColumnIndexOrThrow(_cursor, "toDnsServerName");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfPreviousLatency = CursorUtil.getColumnIndexOrThrow(_cursor, "previousLatency");
          final int _cursorIndexOfNewLatency = CursorUtil.getColumnIndexOrThrow(_cursor, "newLatency");
          final int _cursorIndexOfImprovement = CursorUtil.getColumnIndexOrThrow(_cursor, "improvement");
          final List<DNSSwitchEvent> _result = new ArrayList<DNSSwitchEvent>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DNSSwitchEvent _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpFromDnsServerId;
            _tmpFromDnsServerId = _cursor.getString(_cursorIndexOfFromDnsServerId);
            final String _tmpFromDnsServerName;
            _tmpFromDnsServerName = _cursor.getString(_cursorIndexOfFromDnsServerName);
            final String _tmpToDnsServerId;
            _tmpToDnsServerId = _cursor.getString(_cursorIndexOfToDnsServerId);
            final String _tmpToDnsServerName;
            _tmpToDnsServerName = _cursor.getString(_cursorIndexOfToDnsServerName);
            final SwitchReason _tmpReason;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfReason);
            _tmpReason = __converters.toSwitchReason(_tmp);
            final int _tmpPreviousLatency;
            _tmpPreviousLatency = _cursor.getInt(_cursorIndexOfPreviousLatency);
            final int _tmpNewLatency;
            _tmpNewLatency = _cursor.getInt(_cursorIndexOfNewLatency);
            final int _tmpImprovement;
            _tmpImprovement = _cursor.getInt(_cursorIndexOfImprovement);
            _item = new DNSSwitchEvent(_tmpId,_tmpTimestamp,_tmpFromDnsServerId,_tmpFromDnsServerName,_tmpToDnsServerId,_tmpToDnsServerName,_tmpReason,_tmpPreviousLatency,_tmpNewLatency,_tmpImprovement);
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
  public Object getLatestSwitchEvent(final Continuation<? super DNSSwitchEvent> $completion) {
    final String _sql = "SELECT * FROM dns_switch_events ORDER BY timestamp DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<DNSSwitchEvent>() {
      @Override
      @Nullable
      public DNSSwitchEvent call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfFromDnsServerId = CursorUtil.getColumnIndexOrThrow(_cursor, "fromDnsServerId");
          final int _cursorIndexOfFromDnsServerName = CursorUtil.getColumnIndexOrThrow(_cursor, "fromDnsServerName");
          final int _cursorIndexOfToDnsServerId = CursorUtil.getColumnIndexOrThrow(_cursor, "toDnsServerId");
          final int _cursorIndexOfToDnsServerName = CursorUtil.getColumnIndexOrThrow(_cursor, "toDnsServerName");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfPreviousLatency = CursorUtil.getColumnIndexOrThrow(_cursor, "previousLatency");
          final int _cursorIndexOfNewLatency = CursorUtil.getColumnIndexOrThrow(_cursor, "newLatency");
          final int _cursorIndexOfImprovement = CursorUtil.getColumnIndexOrThrow(_cursor, "improvement");
          final DNSSwitchEvent _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpFromDnsServerId;
            _tmpFromDnsServerId = _cursor.getString(_cursorIndexOfFromDnsServerId);
            final String _tmpFromDnsServerName;
            _tmpFromDnsServerName = _cursor.getString(_cursorIndexOfFromDnsServerName);
            final String _tmpToDnsServerId;
            _tmpToDnsServerId = _cursor.getString(_cursorIndexOfToDnsServerId);
            final String _tmpToDnsServerName;
            _tmpToDnsServerName = _cursor.getString(_cursorIndexOfToDnsServerName);
            final SwitchReason _tmpReason;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfReason);
            _tmpReason = __converters.toSwitchReason(_tmp);
            final int _tmpPreviousLatency;
            _tmpPreviousLatency = _cursor.getInt(_cursorIndexOfPreviousLatency);
            final int _tmpNewLatency;
            _tmpNewLatency = _cursor.getInt(_cursorIndexOfNewLatency);
            final int _tmpImprovement;
            _tmpImprovement = _cursor.getInt(_cursorIndexOfImprovement);
            _result = new DNSSwitchEvent(_tmpId,_tmpTimestamp,_tmpFromDnsServerId,_tmpFromDnsServerName,_tmpToDnsServerId,_tmpToDnsServerName,_tmpReason,_tmpPreviousLatency,_tmpNewLatency,_tmpImprovement);
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
  public Object getSwitchCountSince(final long since,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM dns_switch_events WHERE timestamp >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, since);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
