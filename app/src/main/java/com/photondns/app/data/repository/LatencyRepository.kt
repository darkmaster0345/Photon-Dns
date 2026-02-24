package com.photondns.app.data.repository

import com.photondns.app.data.database.LatencyDao
import com.photondns.app.data.database.SwitchEventDao
import com.photondns.app.data.models.LatencyRecord
import com.photondns.app.data.models.DNSSwitchEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LatencyRepository @Inject constructor(
    private val latencyDao: LatencyDao,
    private val switchEventDao: SwitchEventDao
) {
    
    // Latency Records
    fun getAllLatencyRecords(): Flow<List<LatencyRecord>> = latencyDao.getAllLatencyRecords()
    
    suspend fun getLatencyRecordsForServer(serverId: String, limit: Int = 100): List<LatencyRecord> = 
        latencyDao.getLatencyRecordsForServer(serverId, limit)
    
    suspend fun getLatencyRecordsSince(since: Long): List<LatencyRecord> = 
        latencyDao.getLatencyRecordsSince(since)
    
    suspend fun getLatencyRecordsForServerSince(serverId: String, since: Long): List<LatencyRecord> = 
        latencyDao.getLatencyRecordsForServerSince(serverId, since)
    
    suspend fun getAverageLatencyForServer(serverId: String, since: Long): Double? = 
        latencyDao.getAverageLatencyForServer(serverId, since)
    
    suspend fun getLatestLatencyRecord(): LatencyRecord? = latencyDao.getLatestLatencyRecord()
    
    suspend fun insertLatencyRecord(record: LatencyRecord) = latencyDao.insertLatencyRecord(record)
    
    suspend fun insertLatencyRecords(records: List<LatencyRecord>) = 
        latencyDao.insertLatencyRecords(records)
    
    suspend fun deleteLatencyRecord(record: LatencyRecord) = latencyDao.deleteLatencyRecord(record)
    
    suspend fun deleteOldLatencyRecords(before: Long) = latencyDao.deleteLatencyRecordsBefore(before)
    
    suspend fun deleteAllLatencyRecords() = latencyDao.deleteAllLatencyRecords()
    
    // Switch Events
    fun getAllSwitchEvents(): Flow<List<DNSSwitchEvent>> = switchEventDao.getAllSwitchEvents()
    
    suspend fun getRecentSwitchEvents(limit: Int = 20): List<DNSSwitchEvent> = 
        switchEventDao.getRecentSwitchEvents(limit)
    
    suspend fun getSwitchEventsSince(since: Long): List<DNSSwitchEvent> = 
        switchEventDao.getSwitchEventsSince(since)
    
    suspend fun getLatestSwitchEvent(): DNSSwitchEvent? = switchEventDao.getLatestSwitchEvent()
    
    suspend fun getSwitchCountSince(since: Long): Int = switchEventDao.getSwitchCountSince(since)
    
    suspend fun insertSwitchEvent(event: DNSSwitchEvent) = switchEventDao.insertSwitchEvent(event)
    
    suspend fun deleteSwitchEvent(event: DNSSwitchEvent) = switchEventDao.deleteSwitchEvent(event)
    
    suspend fun deleteOldSwitchEvents(before: Long) = switchEventDao.deleteSwitchEventsBefore(before)
    
    suspend fun deleteAllSwitchEvents() = switchEventDao.deleteAllSwitchEvents()
    
    // Utility methods
    suspend fun recordLatency(
        serverId: String,
        serverName: String,
        serverIp: String,
        latency: Int,
        success: Boolean
    ) {
        val record = LatencyRecord(
            dnsServerId = serverId,
            dnsServerName = serverName,
            dnsServerIp = serverIp,
            latency = latency,
            success = success
        )
        insertLatencyRecord(record)
    }
    
    suspend fun recordSwitchEvent(
        fromServerId: String,
        fromServerName: String,
        toServerId: String,
        toServerName: String,
        reason: com.photondns.app.data.models.SwitchReason,
        previousLatency: Int,
        newLatency: Int
    ) {
        val event = DNSSwitchEvent(
            fromDnsServerId = fromServerId,
            fromDnsServerName = fromServerName,
            toDnsServerId = toServerId,
            toDnsServerName = toServerName,
            reason = reason,
            previousLatency = previousLatency,
            newLatency = newLatency,
            improvement = previousLatency - newLatency
        )
        insertSwitchEvent(event)
    }
}
