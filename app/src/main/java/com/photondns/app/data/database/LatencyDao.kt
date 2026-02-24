package com.photondns.app.data.database

import androidx.room.*
import com.photondns.app.data.models.LatencyRecord
import com.photondns.app.data.models.DNSSwitchEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface LatencyDao {
    
    @Query("SELECT * FROM latency_records ORDER BY timestamp DESC")
    fun getAllLatencyRecords(): Flow<List<LatencyRecord>>
    
    @Query("SELECT * FROM latency_records WHERE dnsServerId = :serverId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatencyRecordsForServer(serverId: String, limit: Int = 100): List<LatencyRecord>
    
    @Query("SELECT * FROM latency_records WHERE timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getLatencyRecordsSince(since: Long): List<LatencyRecord>
    
    @Query("SELECT * FROM latency_records WHERE dnsServerId = :serverId AND timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getLatencyRecordsForServerSince(serverId: String, since: Long): List<LatencyRecord>
    
    @Query("SELECT AVG(latency) as avgLatency FROM latency_records WHERE dnsServerId = :serverId AND success = 1 AND timestamp >= :since")
    suspend fun getAverageLatencyForServer(serverId: String, since: Long): Double?
    
    @Query("SELECT * FROM latency_records ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestLatencyRecord(): LatencyRecord?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLatencyRecord(record: LatencyRecord)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLatencyRecords(records: List<LatencyRecord>)
    
    @Delete
    suspend fun deleteLatencyRecord(record: LatencyRecord)
    
    @Query("DELETE FROM latency_records WHERE timestamp < :before")
    suspend fun deleteLatencyRecordsBefore(before: Long)
    
    @Query("DELETE FROM latency_records")
    suspend fun deleteAllLatencyRecords()
}

@Dao
interface SwitchEventDao {
    
    @Query("SELECT * FROM dns_switch_events ORDER BY timestamp DESC")
    fun getAllSwitchEvents(): Flow<List<DNSSwitchEvent>>
    
    @Query("SELECT * FROM dns_switch_events ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentSwitchEvents(limit: Int = 20): List<DNSSwitchEvent>
    
    @Query("SELECT * FROM dns_switch_events WHERE timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getSwitchEventsSince(since: Long): List<DNSSwitchEvent>
    
    @Query("SELECT * FROM dns_switch_events ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSwitchEvent(): DNSSwitchEvent?
    
    @Query("SELECT COUNT(*) FROM dns_switch_events WHERE timestamp >= :since")
    suspend fun getSwitchCountSince(since: Long): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSwitchEvent(event: DNSSwitchEvent)
    
    @Delete
    suspend fun deleteSwitchEvent(event: DNSSwitchEvent)
    
    @Query("DELETE FROM dns_switch_events WHERE timestamp < :before")
    suspend fun deleteSwitchEventsBefore(before: Long)
    
    @Query("DELETE FROM dns_switch_events")
    suspend fun deleteAllSwitchEvents()
}
