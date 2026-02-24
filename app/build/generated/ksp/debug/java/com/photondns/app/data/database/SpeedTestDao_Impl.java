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
import com.photondns.app.data.models.SpeedTestResult;
import java.lang.Class;
import java.lang.Double;
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
public final class SpeedTestDao_Impl implements SpeedTestDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SpeedTestResult> __insertionAdapterOfSpeedTestResult;

  private final EntityDeletionOrUpdateAdapter<SpeedTestResult> __deletionAdapterOfSpeedTestResult;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSpeedTestsBefore;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllSpeedTests;

  public SpeedTestDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSpeedTestResult = new EntityInsertionAdapter<SpeedTestResult>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `speed_test_results` (`id`,`timestamp`,`downloadSpeed`,`uploadSpeed`,`ping`,`jitter`,`packetLoss`,`testServer`,`dnsUsed`,`testDuration`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SpeedTestResult entity) {
        statement.bindString(1, entity.getId());
        statement.bindLong(2, entity.getTimestamp());
        statement.bindDouble(3, entity.getDownloadSpeed());
        statement.bindDouble(4, entity.getUploadSpeed());
        statement.bindLong(5, entity.getPing());
        statement.bindLong(6, entity.getJitter());
        statement.bindDouble(7, entity.getPacketLoss());
        statement.bindString(8, entity.getTestServer());
        statement.bindString(9, entity.getDnsUsed());
        statement.bindLong(10, entity.getTestDuration());
      }
    };
    this.__deletionAdapterOfSpeedTestResult = new EntityDeletionOrUpdateAdapter<SpeedTestResult>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `speed_test_results` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SpeedTestResult entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteSpeedTestsBefore = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM speed_test_results WHERE timestamp < ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllSpeedTests = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM speed_test_results";
        return _query;
      }
    };
  }

  @Override
  public Object insertSpeedTest(final SpeedTestResult result,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSpeedTestResult.insert(result);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSpeedTest(final SpeedTestResult result,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfSpeedTestResult.handle(result);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSpeedTestsBefore(final long before,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSpeedTestsBefore.acquire();
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
          __preparedStmtOfDeleteSpeedTestsBefore.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllSpeedTests(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllSpeedTests.acquire();
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
          __preparedStmtOfDeleteAllSpeedTests.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SpeedTestResult>> getAllSpeedTests() {
    final String _sql = "SELECT * FROM speed_test_results ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"speed_test_results"}, new Callable<List<SpeedTestResult>>() {
      @Override
      @NonNull
      public List<SpeedTestResult> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfDownloadSpeed = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadSpeed");
          final int _cursorIndexOfUploadSpeed = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadSpeed");
          final int _cursorIndexOfPing = CursorUtil.getColumnIndexOrThrow(_cursor, "ping");
          final int _cursorIndexOfJitter = CursorUtil.getColumnIndexOrThrow(_cursor, "jitter");
          final int _cursorIndexOfPacketLoss = CursorUtil.getColumnIndexOrThrow(_cursor, "packetLoss");
          final int _cursorIndexOfTestServer = CursorUtil.getColumnIndexOrThrow(_cursor, "testServer");
          final int _cursorIndexOfDnsUsed = CursorUtil.getColumnIndexOrThrow(_cursor, "dnsUsed");
          final int _cursorIndexOfTestDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "testDuration");
          final List<SpeedTestResult> _result = new ArrayList<SpeedTestResult>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SpeedTestResult _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final double _tmpDownloadSpeed;
            _tmpDownloadSpeed = _cursor.getDouble(_cursorIndexOfDownloadSpeed);
            final double _tmpUploadSpeed;
            _tmpUploadSpeed = _cursor.getDouble(_cursorIndexOfUploadSpeed);
            final int _tmpPing;
            _tmpPing = _cursor.getInt(_cursorIndexOfPing);
            final int _tmpJitter;
            _tmpJitter = _cursor.getInt(_cursorIndexOfJitter);
            final double _tmpPacketLoss;
            _tmpPacketLoss = _cursor.getDouble(_cursorIndexOfPacketLoss);
            final String _tmpTestServer;
            _tmpTestServer = _cursor.getString(_cursorIndexOfTestServer);
            final String _tmpDnsUsed;
            _tmpDnsUsed = _cursor.getString(_cursorIndexOfDnsUsed);
            final long _tmpTestDuration;
            _tmpTestDuration = _cursor.getLong(_cursorIndexOfTestDuration);
            _item = new SpeedTestResult(_tmpId,_tmpTimestamp,_tmpDownloadSpeed,_tmpUploadSpeed,_tmpPing,_tmpJitter,_tmpPacketLoss,_tmpTestServer,_tmpDnsUsed,_tmpTestDuration);
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
  public Object getRecentSpeedTests(final int limit,
      final Continuation<? super List<SpeedTestResult>> $completion) {
    final String _sql = "SELECT * FROM speed_test_results ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SpeedTestResult>>() {
      @Override
      @NonNull
      public List<SpeedTestResult> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfDownloadSpeed = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadSpeed");
          final int _cursorIndexOfUploadSpeed = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadSpeed");
          final int _cursorIndexOfPing = CursorUtil.getColumnIndexOrThrow(_cursor, "ping");
          final int _cursorIndexOfJitter = CursorUtil.getColumnIndexOrThrow(_cursor, "jitter");
          final int _cursorIndexOfPacketLoss = CursorUtil.getColumnIndexOrThrow(_cursor, "packetLoss");
          final int _cursorIndexOfTestServer = CursorUtil.getColumnIndexOrThrow(_cursor, "testServer");
          final int _cursorIndexOfDnsUsed = CursorUtil.getColumnIndexOrThrow(_cursor, "dnsUsed");
          final int _cursorIndexOfTestDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "testDuration");
          final List<SpeedTestResult> _result = new ArrayList<SpeedTestResult>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SpeedTestResult _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final double _tmpDownloadSpeed;
            _tmpDownloadSpeed = _cursor.getDouble(_cursorIndexOfDownloadSpeed);
            final double _tmpUploadSpeed;
            _tmpUploadSpeed = _cursor.getDouble(_cursorIndexOfUploadSpeed);
            final int _tmpPing;
            _tmpPing = _cursor.getInt(_cursorIndexOfPing);
            final int _tmpJitter;
            _tmpJitter = _cursor.getInt(_cursorIndexOfJitter);
            final double _tmpPacketLoss;
            _tmpPacketLoss = _cursor.getDouble(_cursorIndexOfPacketLoss);
            final String _tmpTestServer;
            _tmpTestServer = _cursor.getString(_cursorIndexOfTestServer);
            final String _tmpDnsUsed;
            _tmpDnsUsed = _cursor.getString(_cursorIndexOfDnsUsed);
            final long _tmpTestDuration;
            _tmpTestDuration = _cursor.getLong(_cursorIndexOfTestDuration);
            _item = new SpeedTestResult(_tmpId,_tmpTimestamp,_tmpDownloadSpeed,_tmpUploadSpeed,_tmpPing,_tmpJitter,_tmpPacketLoss,_tmpTestServer,_tmpDnsUsed,_tmpTestDuration);
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
  public Object getSpeedTestsSince(final long since,
      final Continuation<? super List<SpeedTestResult>> $completion) {
    final String _sql = "SELECT * FROM speed_test_results WHERE timestamp >= ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, since);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SpeedTestResult>>() {
      @Override
      @NonNull
      public List<SpeedTestResult> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfDownloadSpeed = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadSpeed");
          final int _cursorIndexOfUploadSpeed = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadSpeed");
          final int _cursorIndexOfPing = CursorUtil.getColumnIndexOrThrow(_cursor, "ping");
          final int _cursorIndexOfJitter = CursorUtil.getColumnIndexOrThrow(_cursor, "jitter");
          final int _cursorIndexOfPacketLoss = CursorUtil.getColumnIndexOrThrow(_cursor, "packetLoss");
          final int _cursorIndexOfTestServer = CursorUtil.getColumnIndexOrThrow(_cursor, "testServer");
          final int _cursorIndexOfDnsUsed = CursorUtil.getColumnIndexOrThrow(_cursor, "dnsUsed");
          final int _cursorIndexOfTestDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "testDuration");
          final List<SpeedTestResult> _result = new ArrayList<SpeedTestResult>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SpeedTestResult _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final double _tmpDownloadSpeed;
            _tmpDownloadSpeed = _cursor.getDouble(_cursorIndexOfDownloadSpeed);
            final double _tmpUploadSpeed;
            _tmpUploadSpeed = _cursor.getDouble(_cursorIndexOfUploadSpeed);
            final int _tmpPing;
            _tmpPing = _cursor.getInt(_cursorIndexOfPing);
            final int _tmpJitter;
            _tmpJitter = _cursor.getInt(_cursorIndexOfJitter);
            final double _tmpPacketLoss;
            _tmpPacketLoss = _cursor.getDouble(_cursorIndexOfPacketLoss);
            final String _tmpTestServer;
            _tmpTestServer = _cursor.getString(_cursorIndexOfTestServer);
            final String _tmpDnsUsed;
            _tmpDnsUsed = _cursor.getString(_cursorIndexOfDnsUsed);
            final long _tmpTestDuration;
            _tmpTestDuration = _cursor.getLong(_cursorIndexOfTestDuration);
            _item = new SpeedTestResult(_tmpId,_tmpTimestamp,_tmpDownloadSpeed,_tmpUploadSpeed,_tmpPing,_tmpJitter,_tmpPacketLoss,_tmpTestServer,_tmpDnsUsed,_tmpTestDuration);
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
  public Object getLatestSpeedTest(final Continuation<? super SpeedTestResult> $completion) {
    final String _sql = "SELECT * FROM speed_test_results ORDER BY timestamp DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SpeedTestResult>() {
      @Override
      @Nullable
      public SpeedTestResult call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfDownloadSpeed = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadSpeed");
          final int _cursorIndexOfUploadSpeed = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadSpeed");
          final int _cursorIndexOfPing = CursorUtil.getColumnIndexOrThrow(_cursor, "ping");
          final int _cursorIndexOfJitter = CursorUtil.getColumnIndexOrThrow(_cursor, "jitter");
          final int _cursorIndexOfPacketLoss = CursorUtil.getColumnIndexOrThrow(_cursor, "packetLoss");
          final int _cursorIndexOfTestServer = CursorUtil.getColumnIndexOrThrow(_cursor, "testServer");
          final int _cursorIndexOfDnsUsed = CursorUtil.getColumnIndexOrThrow(_cursor, "dnsUsed");
          final int _cursorIndexOfTestDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "testDuration");
          final SpeedTestResult _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final double _tmpDownloadSpeed;
            _tmpDownloadSpeed = _cursor.getDouble(_cursorIndexOfDownloadSpeed);
            final double _tmpUploadSpeed;
            _tmpUploadSpeed = _cursor.getDouble(_cursorIndexOfUploadSpeed);
            final int _tmpPing;
            _tmpPing = _cursor.getInt(_cursorIndexOfPing);
            final int _tmpJitter;
            _tmpJitter = _cursor.getInt(_cursorIndexOfJitter);
            final double _tmpPacketLoss;
            _tmpPacketLoss = _cursor.getDouble(_cursorIndexOfPacketLoss);
            final String _tmpTestServer;
            _tmpTestServer = _cursor.getString(_cursorIndexOfTestServer);
            final String _tmpDnsUsed;
            _tmpDnsUsed = _cursor.getString(_cursorIndexOfDnsUsed);
            final long _tmpTestDuration;
            _tmpTestDuration = _cursor.getLong(_cursorIndexOfTestDuration);
            _result = new SpeedTestResult(_tmpId,_tmpTimestamp,_tmpDownloadSpeed,_tmpUploadSpeed,_tmpPing,_tmpJitter,_tmpPacketLoss,_tmpTestServer,_tmpDnsUsed,_tmpTestDuration);
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
  public Object getAverageDownloadSpeed(final long since,
      final Continuation<? super Double> $completion) {
    final String _sql = "SELECT AVG(downloadSpeed) as avgDownload FROM speed_test_results WHERE timestamp >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
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
  public Object getAverageUploadSpeed(final long since,
      final Continuation<? super Double> $completion) {
    final String _sql = "SELECT AVG(uploadSpeed) as avgUpload FROM speed_test_results WHERE timestamp >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
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
  public Object getAveragePing(final long since, final Continuation<? super Double> $completion) {
    final String _sql = "SELECT AVG(ping) as avgPing FROM speed_test_results WHERE timestamp >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
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
  public Object getSpeedTestCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM speed_test_results";
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
