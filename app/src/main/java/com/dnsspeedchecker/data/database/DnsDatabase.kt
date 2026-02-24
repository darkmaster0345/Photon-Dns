package com.dnsspeedchecker.data.database

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [DnsResult::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DnsDatabase : RoomDatabase() {
    
    abstract fun dnsResultDao(): DnsResultDao
    
    companion object {
        @Volatile
        private var INSTANCE: DnsDatabase? = null
        
        fun getDatabase(context: Context, scope: CoroutineScope): DnsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DnsDatabase::class.java,
                    "dns_speed_checker_database"
                )
                .addCallback(DatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        fun getDatabase(context: Context): DnsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DnsDatabase::class.java,
                    "dns_speed_checker_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
    
    private class DatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    // Database is created, no initial data needed
                }
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): java.util.Date? {
        return value?.let { java.util.Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: java.util.Date?): Long? {
        return date?.time
    }
}
