package com.photondns.app.data.database

import androidx.room.*
import com.photondns.app.data.models.DNSServer
import kotlinx.coroutines.flow.Flow

@Dao
interface DNSServerDao {
    
    @Query("SELECT * FROM dns_servers ORDER BY name ASC")
    fun getAllServers(): Flow<List<DNSServer>>

    @Query("SELECT * FROM dns_servers ORDER BY name ASC")
    suspend fun getAllServersList(): List<DNSServer>

    @Query("SELECT COUNT(*) FROM dns_servers")
    suspend fun getServerCount(): Int
    
    @Query("SELECT * FROM dns_servers WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveServer(): DNSServer?
    
    @Query("SELECT * FROM dns_servers WHERE id = :id LIMIT 1")
    suspend fun getServerById(id: String): DNSServer?
    
    @Query("SELECT * FROM dns_servers ORDER BY latency ASC, name ASC LIMIT :limit")
    suspend fun getFastestServers(limit: Int = 3): List<DNSServer>
    
    @Query("SELECT * FROM dns_servers WHERE latency > 0 ORDER BY latency ASC")
    suspend fun getServersWithLatency(): List<DNSServer>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: DNSServer)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServers(servers: List<DNSServer>)
    
    @Update
    suspend fun updateServer(server: DNSServer)
    
    @Query("UPDATE dns_servers SET isActive = 0 WHERE isActive = 1")
    suspend fun deactivateAllServers()
    
    @Query("UPDATE dns_servers SET latency = :latency WHERE id = :serverId")
    suspend fun updateServerLatency(serverId: String, latency: Int)
    
    @Delete
    suspend fun deleteServer(server: DNSServer)
    
    @Query("DELETE FROM dns_servers WHERE id = :serverId")
    suspend fun deleteServerById(serverId: String)
    
    @Query("DELETE FROM dns_servers WHERE isCustom = 1")
    suspend fun deleteAllCustomServers()
}
