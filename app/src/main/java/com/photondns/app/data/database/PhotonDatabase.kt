package com.photondns.app.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import android.content.Context
import com.photondns.app.data.models.DNSServer
import com.photondns.app.data.models.SpeedTestResult
import com.photondns.app.data.models.LatencyRecord
import com.photondns.app.data.models.DNSSwitchEvent
import com.photondns.app.data.models.SwitchReason

@Database(
    entities = [
        DNSServer::class,
        SpeedTestResult::class,
        LatencyRecord::class,
        DNSSwitchEvent::class
    ],
    version = 1,
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
                .fallbackToDestructiveMigration()
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
}
