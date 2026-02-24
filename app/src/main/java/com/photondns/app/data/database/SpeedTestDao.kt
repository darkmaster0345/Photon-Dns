package com.photondns.app.data.database

import androidx.room.*
import com.photondns.app.data.models.SpeedTestResult
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeedTestDao {
    
    @Query("SELECT * FROM speed_test_results ORDER BY timestamp DESC")
    fun getAllSpeedTests(): Flow<List<SpeedTestResult>>
    
    @Query("SELECT * FROM speed_test_results ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentSpeedTests(limit: Int = 10): List<SpeedTestResult>
    
    @Query("SELECT * FROM speed_test_results WHERE timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getSpeedTestsSince(since: Long): List<SpeedTestResult>
    
    @Query("SELECT * FROM speed_test_results ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSpeedTest(): SpeedTestResult?
    
    @Query("SELECT AVG(downloadSpeed) as avgDownload FROM speed_test_results WHERE timestamp >= :since")
    suspend fun getAverageDownloadSpeed(since: Long): Double?
    
    @Query("SELECT AVG(uploadSpeed) as avgUpload FROM speed_test_results WHERE timestamp >= :since")
    suspend fun getAverageUploadSpeed(since: Long): Double?
    
    @Query("SELECT AVG(ping) as avgPing FROM speed_test_results WHERE timestamp >= :since")
    suspend fun getAveragePing(since: Long): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeedTest(result: SpeedTestResult)
    
    @Delete
    suspend fun deleteSpeedTest(result: SpeedTestResult)
    
    @Query("DELETE FROM speed_test_results WHERE timestamp < :before")
    suspend fun deleteSpeedTestsBefore(before: Long)
    
    @Query("DELETE FROM speed_test_results")
    suspend fun deleteAllSpeedTests()
    
    @Query("SELECT COUNT(*) FROM speed_test_results")
    suspend fun getSpeedTestCount(): Int
}
