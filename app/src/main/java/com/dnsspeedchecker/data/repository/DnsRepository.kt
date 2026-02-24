package com.dnsspeedchecker.data.repository

import com.dnsspeedchecker.data.database.DnsResult
import com.dnsspeedchecker.data.database.DnsResultDao
import kotlinx.coroutines.flow.Flow

interface DnsRepository {
    fun getAllResults(): Flow<List<DnsResult>>
    fun getRecentResultsForServer(serverId: String, limit: Int): Flow<List<DnsResult>>
    suspend fun insertResult(result: DnsResult)
    suspend fun insertResults(results: List<DnsResult>)
    suspend fun getAverageLatencyForServerSince(serverId: String, since: Long): Double?
    suspend fun getRecentLatenciesForServer(serverId: String, limit: Int): List<Long>
    suspend fun cleanupOldResults()
    suspend fun deleteAllResults()
}

class DnsRepositoryImpl(
    private val dnsResultDao: DnsResultDao
) : DnsRepository {
    
    override fun getAllResults(): Flow<List<DnsResult>> {
        return dnsResultDao.getAllResults()
    }
    
    override fun getRecentResultsForServer(serverId: String, limit: Int): Flow<List<DnsResult>> {
        return dnsResultDao.getRecentResultsForServer(serverId, limit)
    }
    
    override suspend fun insertResult(result: DnsResult) {
        dnsResultDao.insertResult(result)
    }
    
    override suspend fun insertResults(results: List<DnsResult>) {
        dnsResultDao.insertResults(results)
    }
    
    override suspend fun getAverageLatencyForServerSince(serverId: String, since: Long): Double? {
        return dnsResultDao.getAverageLatencyForServerSince(serverId, since)
    }
    
    override suspend fun getRecentLatenciesForServer(serverId: String, limit: Int): List<Long> {
        return dnsResultDao.getRecentLatenciesForServer(serverId, limit)
    }
    
    override suspend fun cleanupOldResults() {
        // Delete results older than 7 days
        val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        dnsResultDao.deleteResultsBefore(weekAgo)
    }
    
    override suspend fun deleteAllResults() {
        dnsResultDao.deleteAllResults()
    }
}
