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
import com.photondns.app.data.models.DNSServer;
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
public final class DNSServerDao_Impl implements DNSServerDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DNSServer> __insertionAdapterOfDNSServer;

  private final EntityDeletionOrUpdateAdapter<DNSServer> __deletionAdapterOfDNSServer;

  private final EntityDeletionOrUpdateAdapter<DNSServer> __updateAdapterOfDNSServer;

  private final SharedSQLiteStatement __preparedStmtOfDeactivateAllServers;

  private final SharedSQLiteStatement __preparedStmtOfUpdateServerLatency;

  private final SharedSQLiteStatement __preparedStmtOfDeleteServerById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllCustomServers;

  public DNSServerDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDNSServer = new EntityInsertionAdapter<DNSServer>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `dns_servers` (`id`,`name`,`ip`,`countryCode`,`latency`,`isActive`,`isCustom`,`addedTimestamp`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DNSServer entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getIp());
        statement.bindString(4, entity.getCountryCode());
        statement.bindLong(5, entity.getLatency());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(6, _tmp);
        final int _tmp_1 = entity.isCustom() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        statement.bindLong(8, entity.getAddedTimestamp());
      }
    };
    this.__deletionAdapterOfDNSServer = new EntityDeletionOrUpdateAdapter<DNSServer>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `dns_servers` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DNSServer entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__updateAdapterOfDNSServer = new EntityDeletionOrUpdateAdapter<DNSServer>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `dns_servers` SET `id` = ?,`name` = ?,`ip` = ?,`countryCode` = ?,`latency` = ?,`isActive` = ?,`isCustom` = ?,`addedTimestamp` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DNSServer entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getIp());
        statement.bindString(4, entity.getCountryCode());
        statement.bindLong(5, entity.getLatency());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(6, _tmp);
        final int _tmp_1 = entity.isCustom() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        statement.bindLong(8, entity.getAddedTimestamp());
        statement.bindString(9, entity.getId());
      }
    };
    this.__preparedStmtOfDeactivateAllServers = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE dns_servers SET isActive = 0 WHERE isActive = 1";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateServerLatency = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE dns_servers SET latency = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteServerById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM dns_servers WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllCustomServers = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM dns_servers WHERE isCustom = 1";
        return _query;
      }
    };
  }

  @Override
  public Object insertServer(final DNSServer server, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDNSServer.insert(server);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertServers(final List<DNSServer> servers,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDNSServer.insert(servers);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteServer(final DNSServer server, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfDNSServer.handle(server);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateServer(final DNSServer server, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfDNSServer.handle(server);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deactivateAllServers(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeactivateAllServers.acquire();
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
          __preparedStmtOfDeactivateAllServers.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateServerLatency(final String serverId, final int latency,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateServerLatency.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, latency);
        _argIndex = 2;
        _stmt.bindString(_argIndex, serverId);
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
          __preparedStmtOfUpdateServerLatency.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteServerById(final String serverId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteServerById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, serverId);
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
          __preparedStmtOfDeleteServerById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllCustomServers(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllCustomServers.acquire();
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
          __preparedStmtOfDeleteAllCustomServers.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<DNSServer>> getAllServers() {
    final String _sql = "SELECT * FROM dns_servers ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"dns_servers"}, new Callable<List<DNSServer>>() {
      @Override
      @NonNull
      public List<DNSServer> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfIp = CursorUtil.getColumnIndexOrThrow(_cursor, "ip");
          final int _cursorIndexOfCountryCode = CursorUtil.getColumnIndexOrThrow(_cursor, "countryCode");
          final int _cursorIndexOfLatency = CursorUtil.getColumnIndexOrThrow(_cursor, "latency");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfIsCustom = CursorUtil.getColumnIndexOrThrow(_cursor, "isCustom");
          final int _cursorIndexOfAddedTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "addedTimestamp");
          final List<DNSServer> _result = new ArrayList<DNSServer>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DNSServer _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpIp;
            _tmpIp = _cursor.getString(_cursorIndexOfIp);
            final String _tmpCountryCode;
            _tmpCountryCode = _cursor.getString(_cursorIndexOfCountryCode);
            final int _tmpLatency;
            _tmpLatency = _cursor.getInt(_cursorIndexOfLatency);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final boolean _tmpIsCustom;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsCustom);
            _tmpIsCustom = _tmp_1 != 0;
            final long _tmpAddedTimestamp;
            _tmpAddedTimestamp = _cursor.getLong(_cursorIndexOfAddedTimestamp);
            _item = new DNSServer(_tmpId,_tmpName,_tmpIp,_tmpCountryCode,_tmpLatency,_tmpIsActive,_tmpIsCustom,_tmpAddedTimestamp);
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
  public Object getAllServersList(final Continuation<? super List<DNSServer>> $completion) {
    final String _sql = "SELECT * FROM dns_servers ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DNSServer>>() {
      @Override
      @NonNull
      public List<DNSServer> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfIp = CursorUtil.getColumnIndexOrThrow(_cursor, "ip");
          final int _cursorIndexOfCountryCode = CursorUtil.getColumnIndexOrThrow(_cursor, "countryCode");
          final int _cursorIndexOfLatency = CursorUtil.getColumnIndexOrThrow(_cursor, "latency");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfIsCustom = CursorUtil.getColumnIndexOrThrow(_cursor, "isCustom");
          final int _cursorIndexOfAddedTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "addedTimestamp");
          final List<DNSServer> _result = new ArrayList<DNSServer>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DNSServer _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpIp;
            _tmpIp = _cursor.getString(_cursorIndexOfIp);
            final String _tmpCountryCode;
            _tmpCountryCode = _cursor.getString(_cursorIndexOfCountryCode);
            final int _tmpLatency;
            _tmpLatency = _cursor.getInt(_cursorIndexOfLatency);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final boolean _tmpIsCustom;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsCustom);
            _tmpIsCustom = _tmp_1 != 0;
            final long _tmpAddedTimestamp;
            _tmpAddedTimestamp = _cursor.getLong(_cursorIndexOfAddedTimestamp);
            _item = new DNSServer(_tmpId,_tmpName,_tmpIp,_tmpCountryCode,_tmpLatency,_tmpIsActive,_tmpIsCustom,_tmpAddedTimestamp);
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
  public Object getServerCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM dns_servers";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
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

  @Override
  public Object getActiveServer(final Continuation<? super DNSServer> $completion) {
    final String _sql = "SELECT * FROM dns_servers WHERE isActive = 1 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<DNSServer>() {
      @Override
      @Nullable
      public DNSServer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfIp = CursorUtil.getColumnIndexOrThrow(_cursor, "ip");
          final int _cursorIndexOfCountryCode = CursorUtil.getColumnIndexOrThrow(_cursor, "countryCode");
          final int _cursorIndexOfLatency = CursorUtil.getColumnIndexOrThrow(_cursor, "latency");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfIsCustom = CursorUtil.getColumnIndexOrThrow(_cursor, "isCustom");
          final int _cursorIndexOfAddedTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "addedTimestamp");
          final DNSServer _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpIp;
            _tmpIp = _cursor.getString(_cursorIndexOfIp);
            final String _tmpCountryCode;
            _tmpCountryCode = _cursor.getString(_cursorIndexOfCountryCode);
            final int _tmpLatency;
            _tmpLatency = _cursor.getInt(_cursorIndexOfLatency);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final boolean _tmpIsCustom;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsCustom);
            _tmpIsCustom = _tmp_1 != 0;
            final long _tmpAddedTimestamp;
            _tmpAddedTimestamp = _cursor.getLong(_cursorIndexOfAddedTimestamp);
            _result = new DNSServer(_tmpId,_tmpName,_tmpIp,_tmpCountryCode,_tmpLatency,_tmpIsActive,_tmpIsCustom,_tmpAddedTimestamp);
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
  public Object getServerById(final String id, final Continuation<? super DNSServer> $completion) {
    final String _sql = "SELECT * FROM dns_servers WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<DNSServer>() {
      @Override
      @Nullable
      public DNSServer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfIp = CursorUtil.getColumnIndexOrThrow(_cursor, "ip");
          final int _cursorIndexOfCountryCode = CursorUtil.getColumnIndexOrThrow(_cursor, "countryCode");
          final int _cursorIndexOfLatency = CursorUtil.getColumnIndexOrThrow(_cursor, "latency");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfIsCustom = CursorUtil.getColumnIndexOrThrow(_cursor, "isCustom");
          final int _cursorIndexOfAddedTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "addedTimestamp");
          final DNSServer _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpIp;
            _tmpIp = _cursor.getString(_cursorIndexOfIp);
            final String _tmpCountryCode;
            _tmpCountryCode = _cursor.getString(_cursorIndexOfCountryCode);
            final int _tmpLatency;
            _tmpLatency = _cursor.getInt(_cursorIndexOfLatency);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final boolean _tmpIsCustom;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsCustom);
            _tmpIsCustom = _tmp_1 != 0;
            final long _tmpAddedTimestamp;
            _tmpAddedTimestamp = _cursor.getLong(_cursorIndexOfAddedTimestamp);
            _result = new DNSServer(_tmpId,_tmpName,_tmpIp,_tmpCountryCode,_tmpLatency,_tmpIsActive,_tmpIsCustom,_tmpAddedTimestamp);
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
  public Object getFastestServers(final int limit,
      final Continuation<? super List<DNSServer>> $completion) {
    final String _sql = "SELECT * FROM dns_servers ORDER BY latency ASC, name ASC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DNSServer>>() {
      @Override
      @NonNull
      public List<DNSServer> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfIp = CursorUtil.getColumnIndexOrThrow(_cursor, "ip");
          final int _cursorIndexOfCountryCode = CursorUtil.getColumnIndexOrThrow(_cursor, "countryCode");
          final int _cursorIndexOfLatency = CursorUtil.getColumnIndexOrThrow(_cursor, "latency");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfIsCustom = CursorUtil.getColumnIndexOrThrow(_cursor, "isCustom");
          final int _cursorIndexOfAddedTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "addedTimestamp");
          final List<DNSServer> _result = new ArrayList<DNSServer>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DNSServer _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpIp;
            _tmpIp = _cursor.getString(_cursorIndexOfIp);
            final String _tmpCountryCode;
            _tmpCountryCode = _cursor.getString(_cursorIndexOfCountryCode);
            final int _tmpLatency;
            _tmpLatency = _cursor.getInt(_cursorIndexOfLatency);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final boolean _tmpIsCustom;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsCustom);
            _tmpIsCustom = _tmp_1 != 0;
            final long _tmpAddedTimestamp;
            _tmpAddedTimestamp = _cursor.getLong(_cursorIndexOfAddedTimestamp);
            _item = new DNSServer(_tmpId,_tmpName,_tmpIp,_tmpCountryCode,_tmpLatency,_tmpIsActive,_tmpIsCustom,_tmpAddedTimestamp);
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
  public Object getServersWithLatency(final Continuation<? super List<DNSServer>> $completion) {
    final String _sql = "SELECT * FROM dns_servers WHERE latency > 0 ORDER BY latency ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DNSServer>>() {
      @Override
      @NonNull
      public List<DNSServer> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfIp = CursorUtil.getColumnIndexOrThrow(_cursor, "ip");
          final int _cursorIndexOfCountryCode = CursorUtil.getColumnIndexOrThrow(_cursor, "countryCode");
          final int _cursorIndexOfLatency = CursorUtil.getColumnIndexOrThrow(_cursor, "latency");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfIsCustom = CursorUtil.getColumnIndexOrThrow(_cursor, "isCustom");
          final int _cursorIndexOfAddedTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "addedTimestamp");
          final List<DNSServer> _result = new ArrayList<DNSServer>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DNSServer _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpIp;
            _tmpIp = _cursor.getString(_cursorIndexOfIp);
            final String _tmpCountryCode;
            _tmpCountryCode = _cursor.getString(_cursorIndexOfCountryCode);
            final int _tmpLatency;
            _tmpLatency = _cursor.getInt(_cursorIndexOfLatency);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final boolean _tmpIsCustom;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsCustom);
            _tmpIsCustom = _tmp_1 != 0;
            final long _tmpAddedTimestamp;
            _tmpAddedTimestamp = _cursor.getLong(_cursorIndexOfAddedTimestamp);
            _item = new DNSServer(_tmpId,_tmpName,_tmpIp,_tmpCountryCode,_tmpLatency,_tmpIsActive,_tmpIsCustom,_tmpAddedTimestamp);
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
