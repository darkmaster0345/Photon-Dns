package com.dnsspeedchecker.data.preferences

import kotlinx.serialization.Serializable

/**
 * Comprehensive DNS switching configuration data class
 */
@Serializable
data class DNSSettings(
    val autoSwitchEnabled: Boolean = true,
    val strategy: Strategy = Strategy.BALANCED,
    val customCheckInterval: Int = 5,
    val customMinImprovement: Int = 20,
    val customConsecutiveChecks: Int = 4,
    val customStabilityPeriod: Int = 2,
    val hysteresisEnabled: Boolean = true,
    val hysteresisMargin: Int = 10,
    val switchOnFailure: Boolean = true,
    val batterySaverMode: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    /**
     * Gets the effective check interval based on strategy and battery saver mode
     */
    fun getEffectiveCheckInterval(): Int {
        val baseInterval = when (strategy) {
            Strategy.CONSERVATIVE -> 10
            Strategy.BALANCED -> 5
            Strategy.AGGRESSIVE -> 5
            Strategy.CUSTOM -> customCheckInterval
        }
        
        return if (batterySaverMode) {
            (baseInterval * 1.5).toInt().coerceAtMost(60)
        } else {
            baseInterval
        }
    }
    
    /**
     * Gets the effective minimum improvement based on strategy
     */
    fun getEffectiveMinImprovement(): Int {
        return when (strategy) {
            Strategy.CONSERVATIVE -> 30
            Strategy.BALANCED -> 20
            Strategy.AGGRESSIVE -> 15
            Strategy.CUSTOM -> customMinImprovement
        }
    }
    
    /**
     * Gets the effective consecutive checks needed based on strategy
     */
    fun getEffectiveConsecutiveChecks(): Int {
        return when (strategy) {
            Strategy.CONSERVATIVE -> 5
            Strategy.BALANCED -> 4
            Strategy.AGGRESSIVE -> 2
            Strategy.CUSTOM -> customConsecutiveChecks
        }
    }
    
    /**
     * Gets the effective stability period based on strategy
     */
    fun getEffectiveStabilityPeriod(): Int {
        return when (strategy) {
            Strategy.CONSERVATIVE -> 3
            Strategy.BALANCED -> 2
            Strategy.AGGRESSIVE -> 1
            Strategy.CUSTOM -> customStabilityPeriod
        }
    }
    
    /**
     * Calculates hysteresis threshold to prevent flip-flopping
     */
    fun getHysteresisThreshold(minImprovement: Int): Int {
        return if (hysteresisEnabled) {
            minImprovement + hysteresisMargin
        } else {
            minImprovement
        }
    }
    
    /**
     * Validates settings and returns any issues
     */
    fun validate(): List<String> {
        val issues = mutableListOf<String>()
        
        if (customCheckInterval < 5 || customCheckInterval > 60) {
            issues.add("Check interval must be between 5-60 seconds")
        }
        
        if (customMinImprovement < 10 || customMinImprovement > 100) {
            issues.add("Minimum improvement must be between 10-100ms")
        }
        
        if (customConsecutiveChecks < 2 || customConsecutiveChecks > 10) {
            issues.add("Consecutive checks must be between 2-10")
        }
        
        if (customStabilityPeriod < 1 || customStabilityPeriod > 10) {
            issues.add("Stability period must be between 1-10 minutes")
        }
        
        if (hysteresisMargin < 5 || hysteresisMargin > 50) {
            issues.add("Hysteresis margin must be between 5-50ms")
        }
        
        return issues
    }
}

/**
 * DNS switching strategy enum
 */
@Serializable
enum class Strategy {
    CONSERVATIVE,
    BALANCED,
    AGGRESSIVE,
    CUSTOM;
    
    /**
     * Gets display name for the strategy
     */
    fun getDisplayName(): String {
        return when (this) {
            CONSERVATIVE -> "Conservative"
            BALANCED -> "Balanced"
            AGGRESSIVE -> "Aggressive"
            CUSTOM -> "Custom"
        }
    }
    
    /**
     * Gets description for the strategy
     */
    fun getDescription(): String {
        return when (this) {
            CONSERVATIVE -> "Prioritizes stability over speed. Fewer switches, more reliable."
            BALANCED -> "Balanced approach between speed and stability. Good for most users."
            AGGRESSIVE -> "Prioritizes speed over stability. More frequent switches for optimal performance."
            CUSTOM -> "Custom configuration tailored to your preferences."
        }
    }
    
    /**
     * Gets recommended use case
     */
    fun getRecommendedFor(): String {
        return when (this) {
            CONSERVATIVE -> "Recommended for: Stable networks, business users, conservative approach"
            BALANCED -> "Recommended for: Daily use, mixed network conditions, most users"
            AGGRESSIVE -> "Recommended for: Gaming, streaming, variable networks, power users"
            CUSTOM -> "Recommended for: Advanced users who want fine-tuned control"
        }
    }
}
