package com.photondns.app.data.database;

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
public final class PhotonDatabase_Impl extends PhotonDatabase {
  private volatile DNSServerDao _dNSServerDao;

  private volatile SpeedTestDao _speedTestDao;

  private volatile LatencyDao _latencyDao;

  private volatile SwitchEventDao _switchEventDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `dns_servers` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `ip` TEXT NOT NULL, `countryCode` TEXT NOT NULL, `latency` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `isCustom` INTEGER NOT NULL, `addedTimestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `speed_test_results` (`id` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `downloadSpeed` REAL NOT NULL, `uploadSpeed` REAL NOT NULL, `ping` INTEGER NOT NULL, `jitter` INTEGER NOT NULL, `packetLoss` REAL NOT NULL, `testServer` TEXT NOT NULL, `dnsUsed` TEXT NOT NULL, `testDuration` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `latency_records` (`id` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `dnsServerId` TEXT NOT NULL, `dnsServerName` TEXT NOT NULL, `dnsServerIp` TEXT NOT NULL, `latency` INTEGER NOT NULL, `success` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `dns_switch_events` (`id` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `fromDnsServerId` TEXT NOT NULL, `fromDnsServerName` TEXT NOT NULL, `toDnsServerId` TEXT NOT NULL, `toDnsServerName` TEXT NOT NULL, `reason` TEXT NOT NULL, `previousLatency` INTEGER NOT NULL, `newLatency` INTEGER NOT NULL, `improvement` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '705b94bc93a6a5508a7e4fb4c0a14cf1')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `dns_servers`");
        db.execSQL("DROP TABLE IF EXISTS `speed_test_results`");
        db.execSQL("DROP TABLE IF EXISTS `latency_records`");
        db.execSQL("DROP TABLE IF EXISTS `dns_switch_events`");
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
        final HashMap<String, TableInfo.Column> _columnsDnsServers = new HashMap<String, TableInfo.Column>(8);
        _columnsDnsServers.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDnsServers.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDnsServers.put("ip", new TableInfo.Column("ip", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDnsServers.put("countryCode", new TableInfo.Column("countryCode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDnsServers.put("latency", new TableInfo.Column("latency", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDnsServers.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDnsServers.put("isCustom", new TableInfo.Column("isCustom", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDnsServers.put("addedTimestamp", new TableInfo.Column("addedTimestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDnsServers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDnsServers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDnsServers = new TableInfo("dns_servers", _columnsDnsServers, _foreignKeysDnsServers, _indicesDnsServers);
        final TableInfo _existingDnsServers = TableInfo.read(db, "dns_servers");
        if (!_infoDnsServers.equals(_existingDnsServers)) {
          return new RoomOpenHelper.ValidationResult(false, "dns_servers(com.photondns.app.data.models.DNSServer).\n"
                  + " Expected:\n" + _infoDnsServers + "\n"
                  + " Found:\n" + _existingDnsServers);
        }
        final HashMap<String, TableInfo.Column> _columnsSpeedTestResults = new HashMap<String, TableInfo.Column>(10);
        _columnsSpeedTestResults.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSpeedTestResults.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSpeedTestResults.put("downloadSpeed", new TableInfo.Column("downloadSpeed", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSpeedTestResults.put("uploadSpeed", new TableInfo.Column("uploadSpeed", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSpeedTestResults.put("ping", new TableInfo.Column("ping", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSpeedTestResults.put("jitter", new TableInfo.Column("jitter", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSpeedTestResults.put("packetLoss", new TableInfo.Column("packetLoss", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSpeedTestResults.put("testServer", new TableInfo.Column("testServer", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSpeedTestResults.put("dnsUsed", new TableInfo.Column("dnsUsed", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSpeedTestResults.put("testDuration", new TableInfo.Column("testDuration", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSpeedTestResults = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSpeedTestResults = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSpeedTestResults = new TableInfo("speed_test_results", _columnsSpeedTestResults, _foreignKeysSpeedTestResults, _indicesSpeedTestResults);
        final TableInfo _existingSpeedTestResults = TableInfo.read(db, "speed_test_results");
        if (!_infoSpeedTestResults.equals(_existingSpeedTestResults)) {
          return new RoomOpenHelper.ValidationResult(false, "speed_test_results(com.photondns.app.data.models.SpeedTestResult).\n"
                  + " Expected:\n" + _infoSpeedTestResults + "\n"
                  + " Found:\n" + _existingSpeedTestResults);
        }
        final HashMap<String, TableInfo.Column> _columnsLatencyRecords = new HashMap<String, TableInfo.Column>(7);
        _columnsLatencyRecords.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLatencyRecords.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLatencyRecords.put("dnsServerId", new TableInfo.Column("dnsServerId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLatencyRecords.put("dnsServerName", new TableInfo.Column("dnsServerName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLatencyRecords.put("dnsServerIp", new TableInfo.Column("dnsServerIp", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLatencyRecords.put("latency", new TableInfo.Column("latency", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLatencyRecords.put("success", new TableInfo.Column("success", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLatencyRecords = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesLatencyRecords = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLatencyRecords = new TableInfo("latency_records", _columnsLatencyRecords, _foreignKeysLatencyRecords, _indicesLatencyRecords);
        final TableInfo _existingLatencyRecords = TableInfo.read(db, "latency_records");
        if (!_infoLatencyRecords.equals(_existingLatencyRecords)) {
          return new RoomOpenHelper.ValidationResult(false, "latency_records(com.photondns.app.data.models.LatencyRecord).\n"
                  + " Expected:\n" + _infoLatencyRecords + "\n"
                  + " Found:\n" + _existingLatencyRecords);
        }
        final HashMap<String, TableInfo.Column> _columnsDnsSwitchEvents = new HashMap<String, TableInfo.Column>(10);
        _columnsDnsSwitchEvents.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDnsSwitchEvents.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDnsSwitchEvents.put("fromDnsServerId", new TableInfo.Column("fromDnsServerId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDnsSwitchEvents.put("fromDnsServerName", new TableInfo.Column("fromDnsServerName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDnsSwitchEvents.put("toDnsServerId", new TableInfo.Column("toDnsServerId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDnsSwitchEvents.put("toDnsServerName", new TableInfo.Column("toDnsServerName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDnsSwitchEvents.put("reason", new TableInfo.Column("reason", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDnsSwitchEvents.put("previousLatency", new TableInfo.Column("previousLatency", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDnsSwitchEvents.put("newLatency", new TableInfo.Column("newLatency", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDnsSwitchEvents.put("improvement", new TableInfo.Column("improvement", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDnsSwitchEvents = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDnsSwitchEvents = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDnsSwitchEvents = new TableInfo("dns_switch_events", _columnsDnsSwitchEvents, _foreignKeysDnsSwitchEvents, _indicesDnsSwitchEvents);
        final TableInfo _existingDnsSwitchEvents = TableInfo.read(db, "dns_switch_events");
        if (!_infoDnsSwitchEvents.equals(_existingDnsSwitchEvents)) {
          return new RoomOpenHelper.ValidationResult(false, "dns_switch_events(com.photondns.app.data.models.DNSSwitchEvent).\n"
                  + " Expected:\n" + _infoDnsSwitchEvents + "\n"
                  + " Found:\n" + _existingDnsSwitchEvents);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "705b94bc93a6a5508a7e4fb4c0a14cf1", "c5d3ec0f40c929ce10e3a2ce5a0d244c");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "dns_servers","speed_test_results","latency_records","dns_switch_events");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `dns_servers`");
      _db.execSQL("DELETE FROM `speed_test_results`");
      _db.execSQL("DELETE FROM `latency_records`");
      _db.execSQL("DELETE FROM `dns_switch_events`");
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
    _typeConvertersMap.put(DNSServerDao.class, DNSServerDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SpeedTestDao.class, SpeedTestDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(LatencyDao.class, LatencyDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SwitchEventDao.class, SwitchEventDao_Impl.getRequiredConverters());
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
  public DNSServerDao dnsServerDao() {
    if (_dNSServerDao != null) {
      return _dNSServerDao;
    } else {
      synchronized(this) {
        if(_dNSServerDao == null) {
          _dNSServerDao = new DNSServerDao_Impl(this);
        }
        return _dNSServerDao;
      }
    }
  }

  @Override
  public SpeedTestDao speedTestDao() {
    if (_speedTestDao != null) {
      return _speedTestDao;
    } else {
      synchronized(this) {
        if(_speedTestDao == null) {
          _speedTestDao = new SpeedTestDao_Impl(this);
        }
        return _speedTestDao;
      }
    }
  }

  @Override
  public LatencyDao latencyDao() {
    if (_latencyDao != null) {
      return _latencyDao;
    } else {
      synchronized(this) {
        if(_latencyDao == null) {
          _latencyDao = new LatencyDao_Impl(this);
        }
        return _latencyDao;
      }
    }
  }

  @Override
  public SwitchEventDao switchEventDao() {
    if (_switchEventDao != null) {
      return _switchEventDao;
    } else {
      synchronized(this) {
        if(_switchEventDao == null) {
          _switchEventDao = new SwitchEventDao_Impl(this);
        }
        return _switchEventDao;
      }
    }
  }
}
