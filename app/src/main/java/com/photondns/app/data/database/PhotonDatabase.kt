package com.photondns.app.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
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

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Migration from version 1 to 2
                // Add protocol-specific fields if they don't exist
                database.execSQL("ALTER TABLE dns_servers ADD COLUMN protocol TEXT NOT NULL DEFAULT 'UDP'")
                database.execSQL("ALTER TABLE dns_servers ADD COLUMN dohUrl TEXT")
                database.execSQL("ALTER TABLE dns_servers ADD COLUMN dotHostname TEXT")
            }
        }

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
    fun toDNSProtocol(protocol: String?): DNSProtocol {
        if (protocol == null) return DNSProtocol.UDP
        return try {
            DNSProtocol.valueOf(protocol)
        } catch (e: IllegalArgumentException) {
            DNSProtocol.UDP
        }
    }
}
