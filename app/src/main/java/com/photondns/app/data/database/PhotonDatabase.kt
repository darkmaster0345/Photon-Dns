package com.photondns.app.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.photondns.app.data.models.DNSServer
import com.photondns.app.data.models.SpeedTestResult
import com.photondns.app.data.models.LatencyRecord
import com.photondns.app.data.models.DNSSwitchEvent
import com.photondns.app.data.models.SwitchReason
import com.photondns.app.data.models.DNSProtocol

@Database(
    entities = [
        DNSServer::class,
        SpeedTestResult::class,
        LatencyRecord::class,
        DNSSwitchEvent::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PhotonDatabase : RoomDatabase() {
    
    abstract fun dnsServerDao(): DNSServerDao
    abstract fun speedTestDao(): SpeedTestDao
    abstract fun latencyDao(): LatencyDao
    abstract fun switchEventDao(): SwitchEventDao
    
    companion object {
        @Volatile
        private var INSTANCE: PhotonDatabase? = null
        
        fun getDatabase(context: Context): PhotonDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PhotonDatabase::class.java,
                    "photon_dns_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * Migration from version 1 to 2.
 *
 * Schema changes in version 2:
 * - DNSServer: Added `protocol` column (DNSProtocol enum, defaults to UDP)
 * - DNSServer: Added `dohUrl` column (nullable String for DoH endpoints)
 * - DNSServer: Added `dotHostname` column (nullable String for DoT hostnames)
 * - SpeedTestResult: Added `bufferbloat` column (Integer, defaults to 0)
 * - SpeedTestResult: Added `privacyScore` column (Integer, defaults to 100)
 * - SwitchReason: Added new enum values (PROTOCOL_UPGRADE, LATENCY_THRESHOLD, SECURITY_FILTER)
 *   These new enum values don't require schema changes as they're stored as strings.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE dns_servers ADD COLUMN protocol TEXT NOT NULL DEFAULT 'UDP'"
        )
        database.execSQL(
            "ALTER TABLE dns_servers ADD COLUMN dohUrl TEXT DEFAULT NULL"
        )
        database.execSQL(
            "ALTER TABLE dns_servers ADD COLUMN dotHostname TEXT DEFAULT NULL"
        )
        database.execSQL(
            "ALTER TABLE speed_test_results ADD COLUMN bufferbloat INTEGER NOT NULL DEFAULT 0"
        )
        database.execSQL(
            "ALTER TABLE speed_test_results ADD COLUMN privacyScore INTEGER NOT NULL DEFAULT 100"
        )
    }
}

class Converters {
    @TypeConverter
    fun fromSwitchReason(switchReason: SwitchReason): String {
        return switchReason.name
    }
    
    @TypeConverter
    fun toSwitchReason(switchReason: String): SwitchReason {
        return SwitchReason.valueOf(switchReason)
    }

    @TypeConverter
    fun fromDNSProtocol(protocol: DNSProtocol): String {
        return protocol.name
    }

    @TypeConverter
    fun toDNSProtocol(protocol: String): DNSProtocol {
        return DNSProtocol.valueOf(protocol)
    }
}
