package com.photondns.app.data.repository

import com.photondns.app.data.database.DNSServerDao
import com.photondns.app.data.models.DNSServer
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DNSServerRepository @Inject constructor(
    private val dnsServerDao: DNSServerDao
) {
    
    fun getAllServers(): Flow<List<DNSServer>> = dnsServerDao.getAllServers()

    suspend fun getAllServersList(): List<DNSServer> = dnsServerDao.getAllServersList()
    
    suspend fun getActiveServer(): DNSServer? = dnsServerDao.getActiveServer()
    
    suspend fun getServerById(id: String): DNSServer? = dnsServerDao.getServerById(id)
    
    suspend fun getFastestServers(limit: Int = 3): List<DNSServer> = 
        dnsServerDao.getFastestServers(limit)
    
    suspend fun getServersWithLatency(): List<DNSServer> = 
        dnsServerDao.getServersWithLatency()
    
    suspend fun insertServer(server: DNSServer) = dnsServerDao.insertServer(server)
    
    suspend fun insertServers(servers: List<DNSServer>) = dnsServerDao.insertServers(servers)
    
    suspend fun updateServer(server: DNSServer) = dnsServerDao.updateServer(server)
    
    suspend fun setActiveServer(serverId: String) {
        val server = dnsServerDao.getServerById(serverId) ?: return
        dnsServerDao.deactivateAllServers()
        dnsServerDao.updateServer(server.copy(isActive = true))
    }
    
    suspend fun updateServerLatency(serverId: String, latency: Int) = 
        dnsServerDao.updateServerLatency(serverId, latency)
    
    suspend fun deleteServer(server: DNSServer) = dnsServerDao.deleteServer(server)
    
    suspend fun deleteServerById(serverId: String) = dnsServerDao.deleteServerById(serverId)
    
    suspend fun deleteAllCustomServers() = dnsServerDao.deleteAllCustomServers()
    
    suspend fun initializeDefaultServers() {
        val defaultServers = DNSServer.getPredefinedServers().mapIndexed { index, server ->
            if (index == 0) server.copy(isActive = true) else server
        }
        dnsServerDao.insertServers(defaultServers)
    }

    suspend fun ensureDefaultServersInitialized() {
        val serverCount = dnsServerDao.getServerCount()
        if (serverCount == 0) {
            initializeDefaultServers()
            return
        }

        if (dnsServerDao.getActiveServer() == null) {
            dnsServerDao.getAllServersList().firstOrNull()?.let { firstServer ->
                setActiveServer(firstServer.id)
            }
        }
    }
}
