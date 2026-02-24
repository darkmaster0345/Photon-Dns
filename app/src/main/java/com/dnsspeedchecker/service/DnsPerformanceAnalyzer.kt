package com.dnsspeedchecker.service

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*

/**
 * Advanced DNS performance analyzer for intelligent switching decisions
 */
class DnsPerformanceAnalyzer {
    
    companion object {
        private const val TAG = "DnsPerformanceAnalyzer"
        private const val MIN_SAMPLES_FOR_ANALYSIS = 3
        private const val STABILITY_THRESHOLD = 0.3 // 30% variance threshold
        private const val TREND_ANALYSIS_SAMPLES = 5
    }
    
    data class PerformanceMetrics(
        val avgLatency: Double,
        val medianLatency: Double,
        val stdDeviation: Double,
        val variance: Double,
        val stabilityScore: Double, // 0.0 (unstable) to 1.0 (stable)
        val trend: Trend, // IMPROVING, STABLE, DEGRADING
        val reliability: Double, // 0.0 to 1.0 based on success rate
        val performanceScore: Double // Overall score (0.0 to 1.0)
    )
    
    enum class Trend {
        IMPROVING, STABLE, DEGRADING, UNKNOWN
    }
    
    /**
     * Analyzes DNS performance from latency history
     */
    suspend fun analyzePerformance(latencyHistory: List<Long>, successRate: Float = 1.0f): PerformanceMetrics {
        return withContext(Dispatchers.Default) {
            if (latencyHistory.size < MIN_SAMPLES_FOR_ANALYSIS) {
                return@withContext createDefaultMetrics(latencyHistory, successRate)
            }
            
            val validLatencies = latencyHistory.filter { it > 0 }
            if (validLatencies.isEmpty()) {
                return@withContext createDefaultMetrics(latencyHistory, successRate)
            }
            
            // Calculate basic statistics
            val avgLatency = validLatencies.average()
            val medianLatency = calculateMedian(validLatencies)
            val variance = calculateVariance(validLatencies, avgLatency)
            val stdDeviation = sqrt(variance)
            
            // Calculate stability score (lower variance = higher stability)
            val stabilityScore = calculateStabilityScore(avgLatency, stdDeviation)
            
            // Analyze trend
            val trend = analyzeTrend(validLatencies)
            
            // Calculate reliability based on success rate and consistency
            val reliability = calculateReliability(successRate, stabilityScore)
            
            // Calculate overall performance score
            val performanceScore = calculatePerformanceScore(
                avgLatency, stabilityScore, reliability, trend
            )
            
            PerformanceMetrics(
                avgLatency = avgLatency,
                medianLatency = medianLatency,
                stdDeviation = stdDeviation,
                variance = variance,
                stabilityScore = stabilityScore,
                trend = trend,
                reliability = reliability,
                performanceScore = performanceScore
            )
        }
    }
    
    /**
     * Compares two DNS servers and determines if switching is recommended
     */
    suspend fun shouldSwitch(
        currentMetrics: PerformanceMetrics,
        newMetrics: PerformanceMetrics,
        thresholdMs: Long
    ): SwitchRecommendation {
        return withContext(Dispatchers.Default) {
            val latencyImprovement = currentMetrics.avgLatency - newMetrics.avgLatency
            val performanceImprovement = newMetrics.performanceScore - currentMetrics.performanceScore
            
            val shouldSwitch = when {
                // Significant latency improvement
                latencyImprovement >= thresholdMs -> true
                
                // Better overall performance with reasonable latency improvement
                performanceImprovement >= 0.1 && latencyImprovement >= thresholdMs / 2 -> true
                
                // Current server is unreliable and new one is significantly better
                currentMetrics.reliability < 0.5 && newMetrics.reliability > 0.8 -> true
                
                // Current server is unstable and new one is much more stable
                currentMetrics.stabilityScore < 0.3 && newMetrics.stabilityScore > 0.7 -> true
                
                else -> false
            }
            
            val confidence = calculateSwitchConfidence(currentMetrics, newMetrics, latencyImprovement)
            val reason = generateSwitchReason(currentMetrics, newMetrics, latencyImprovement, performanceImprovement)
            
            SwitchRecommendation(
                shouldSwitch = shouldSwitch,
                confidence = confidence,
                reason = reason,
                latencyImprovement = latencyImprovement,
                performanceImprovement = performanceImprovement
            )
        }
    }
    
    data class SwitchRecommendation(
        val shouldSwitch: Boolean,
        val confidence: Double, // 0.0 to 1.0
        val reason: String,
        val latencyImprovement: Double,
        val performanceImprovement: Double
    )
    
    private fun createDefaultMetrics(latencyHistory: List<Long>, successRate: Float): PerformanceMetrics {
        val avgLatency = if (latencyHistory.isNotEmpty()) latencyHistory.average() else 0.0
        return PerformanceMetrics(
            avgLatency = avgLatency,
            medianLatency = avgLatency,
            stdDeviation = 0.0,
            variance = 0.0,
            stabilityScore = if (successRate > 0.8f) 0.5 else 0.2,
            trend = Trend.UNKNOWN,
            reliability = successRate.toDouble(),
            performanceScore = calculateSimplePerformanceScore(avgLatency, successRate.toDouble())
        )
    }
    
    private fun calculateMedian(values: List<Long>): Double {
        if (values.isEmpty()) return 0.0
        
        val sorted = values.sorted()
        val middle = sorted.size / 2
        
        return if (sorted.size % 2 == 0) {
            (sorted[middle - 1] + sorted[middle]) / 2.0
        } else {
            sorted[middle].toDouble()
        }
    }
    
    private fun calculateVariance(values: List<Long>, mean: Double): Double {
        if (values.isEmpty()) return 0.0
        
        return values.map { (it - mean).pow(2) }.average()
    }
    
    private fun calculateStabilityScore(mean: Double, stdDev: Double): Double {
        if (mean == 0.0) return 0.0
        
        val coefficientOfVariation = stdDev / mean
        return (1.0 - min(coefficientOfVariation, 1.0)).coerceAtLeast(0.0)
    }
    
    private fun analyzeTrend(values: List<Long>): Trend {
        if (values.size < TREND_ANALYSIS_SAMPLES) return Trend.UNKNOWN
        
        val recentValues = values.takeLast(TREND_ANALYSIS_SAMPLES)
        val firstHalf = recentValues.take(recentValues.size / 2)
        val secondHalf = recentValues.drop(recentValues.size / 2)
        
        val firstAvg = firstHalf.average()
        val secondAvg = secondHalf.average()
        
        val change = (secondAvg - firstAvg) / firstAvg
        
        return when {
            change > 0.1 -> Trend.DEGRADING
            change < -0.1 -> Trend.IMPROVING
            else -> Trend.STABLE
        }
    }
    
    private fun calculateReliability(successRate: Float, stabilityScore: Double): Double {
        return (successRate.toDouble() * 0.7 + stabilityScore * 0.3).coerceIn(0.0, 1.0)
    }
    
    private fun calculatePerformanceScore(
        avgLatency: Double,
        stabilityScore: Double,
        reliability: Double,
        trend: Trend
    ): Double {
        // Base score from latency (lower is better)
        val latencyScore = when {
            avgLatency <= 20 -> 1.0
            avgLatency <= 50 -> 0.8
            avgLatency <= 100 -> 0.6
            avgLatency <= 200 -> 0.4
            else -> 0.2
        }
        
        // Trend bonus/penalty
        val trendModifier = when (trend) {
            Trend.IMPROVING -> 0.1
            Trend.STABLE -> 0.0
            Trend.DEGRADING -> -0.1
            Trend.UNKNOWN -> 0.0
        }
        
        // Combine all factors
        val rawScore = (latencyScore * 0.5 + stabilityScore * 0.2 + reliability * 0.3 + trendModifier)
        return rawScore.coerceIn(0.0, 1.0)
    }
    
    private fun calculateSimplePerformanceScore(avgLatency: Double, reliability: Double): Double {
        val latencyScore = when {
            avgLatency <= 20 -> 1.0
            avgLatency <= 50 -> 0.8
            avgLatency <= 100 -> 0.6
            avgLatency <= 200 -> 0.4
            else -> 0.2
        }
        
        return (latencyScore * 0.7 + reliability * 0.3).coerceIn(0.0, 1.0)
    }
    
    private fun calculateSwitchConfidence(
        current: PerformanceMetrics,
        new: PerformanceMetrics,
        latencyImprovement: Double
    ): Double {
        val factors = mutableListOf<Double>()
        
        // Latency improvement factor
        if (latencyImprovement > 0) {
            factors.add(min(latencyImprovement / 100.0, 1.0))
        }
        
        // Performance improvement factor
        val performanceDiff = new.performanceScore - current.performanceScore
        if (performanceDiff > 0) {
            factors.add(performanceDiff)
        }
        
        // Reliability improvement factor
        val reliabilityDiff = new.reliability - current.reliability
        if (reliabilityDiff > 0) {
            factors.add(reliabilityDiff * 0.5)
        }
        
        // Stability improvement factor
        val stabilityDiff = new.stabilityScore - current.stabilityScore
        if (stabilityDiff > 0) {
            factors.add(stabilityDiff * 0.3)
        }
        
        return if (factors.isNotEmpty()) {
            factors.average().coerceIn(0.0, 1.0)
        } else {
            0.0
        }
    }
    
    private fun generateSwitchReason(
        current: PerformanceMetrics,
        new: PerformanceMetrics,
        latencyImprovement: Double,
        performanceImprovement: Double
    ): String {
        val reasons = mutableListOf<String>()
        
        if (latencyImprovement >= 20) {
            reasons.add("significant latency improvement (${latencyImprovement.toInt()}ms)")
        }
        
        if (performanceImprovement >= 0.1) {
            reasons.add("better overall performance")
        }
        
        if (new.reliability > current.reliability + 0.2) {
            reasons.add("more reliable")
        }
        
        if (new.stabilityScore > current.stabilityScore + 0.3) {
            reasons.add("more stable")
        }
        
        return if (reasons.isNotEmpty()) {
            reasons.joinToString(", ")
        } else {
            "marginal improvement"
        }
    }
}
