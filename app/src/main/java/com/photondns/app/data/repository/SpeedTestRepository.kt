package com.photondns.app.data.repository

import com.photondns.app.data.database.SpeedTestDao
import com.photondns.app.data.models.SpeedTestResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeedTestRepository @Inject constructor(
    private val speedTestDao: SpeedTestDao
) {
    
    fun getAllSpeedTests(): Flow<List<SpeedTestResult>> = speedTestDao.getAllSpeedTests()
    
    suspend fun getRecentSpeedTests(limit: Int = 10): List<SpeedTestResult> = 
        speedTestDao.getRecentSpeedTests(limit)
    
    suspend fun getSpeedTestsSince(since: Long): List<SpeedTestResult> = 
        speedTestDao.getSpeedTestsSince(since)
    
    suspend fun getLatestSpeedTest(): SpeedTestResult? = speedTestDao.getLatestSpeedTest()
    
    suspend fun getAverageDownloadSpeed(since: Long): Double? = 
        speedTestDao.getAverageDownloadSpeed(since)
    
    suspend fun getAverageUploadSpeed(since: Long): Double? = 
        speedTestDao.getAverageUploadSpeed(since)
    
    suspend fun getAveragePing(since: Long): Double? = 
        speedTestDao.getAveragePing(since)
    
    suspend fun insertSpeedTest(result: SpeedTestResult) = speedTestDao.insertSpeedTest(result)
    
    suspend fun deleteSpeedTest(result: SpeedTestResult) = speedTestDao.deleteSpeedTest(result)
    
    suspend fun deleteOldSpeedTests(before: Long) = speedTestDao.deleteSpeedTestsBefore(before)
    
    suspend fun deleteAllSpeedTests() = speedTestDao.deleteAllSpeedTests()
    
    suspend fun getSpeedTestCount(): Int = speedTestDao.getSpeedTestCount()
    
    // Utility methods for statistics
    suspend fun getSpeedTestStatsForPeriod(since: Long): SpeedTestStats? {
        val avgDownload = getAverageDownloadSpeed(since)
        val avgUpload = getAverageUploadSpeed(since)
        val avgPing = getAveragePing(since)
        val testCount = getSpeedTestCount()
        
        return if (avgDownload != null && avgUpload != null && avgPing != null) {
            SpeedTestStats(
                averageDownloadSpeed = avgDownload,
                averageUploadSpeed = avgUpload,
                averagePing = avgPing,
                testCount = testCount
            )
        } else null
    }
}

data class SpeedTestStats(
    val averageDownloadSpeed: Double,
    val averageUploadSpeed: Double,
    val averagePing: Double,
    val testCount: Int
)
