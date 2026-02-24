package com.dnsspeedchecker.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DnsResultDao {
    
    @Query("SELECT * FROM dns_results ORDER BY timestamp DESC")
    fun getAllResults(): Flow<List<DnsResult>>
    
    @Query("SELECT * FROM dns_results WHERE serverId = :serverId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentResultsForServer(serverId: String, limit: Int = 5): Flow<List<DnsResult>>
    
    @Query("SELECT * FROM dns_results WHERE timestamp >= :since ORDER BY timestamp DESC")
    fun getResultsSince(since: Long): Flow<List<DnsResult>>
    
    @Query("SELECT AVG(latencyMs) FROM dns_results WHERE serverId = :serverId AND timestamp >= :since AND success = 1")
    suspend fun getAverageLatencyForServerSince(serverId: String, since: Long): Double?
    
    @Query("SELECT latencyMs FROM dns_results WHERE serverId = :serverId AND success = 1 ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentLatenciesForServer(serverId: String, limit: Int = 5): List<Long>
    
    @Insert
    suspend fun insertResult(result: DnsResult)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<DnsResult>)
    
    @Query("DELETE FROM dns_results WHERE timestamp < :before")
    suspend fun deleteResultsBefore(before: Long)
    
    @Query("DELETE FROM dns_results")
    suspend fun deleteAllResults()
    
    @Query("SELECT COUNT(*) FROM dns_results")
    suspend fun getResultCount(): Int
}
