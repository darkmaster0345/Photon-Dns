package com.dnsspeedchecker.service

import android.util.Log
import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/**
 * Utility class for preventing memory leaks in background services
 */
object MemoryLeakPrevention {
    
    private const val TAG = "MemoryLeakPrevention"
    
    // Weak reference cache for context and other objects
    private val weakReferences = ConcurrentHashMap<String, WeakReference<*>>()
    
    // Track active operations for cleanup
    private val activeOperations = ConcurrentHashMap<String, Long>()
    
    /**
     * Store weak reference to prevent memory leaks
     */
    fun storeWeakReference(key: String, obj: Any?) {
        if (obj != null) {
            weakReferences[key] = WeakReference(obj)
        } else {
            weakReferences.remove(key)
        }
    }
    
    /**
     * Get object from weak reference
     */
    fun <T> getWeakReference(key: String, clazz: Class<T>): T? {
        val weakRef = weakReferences[key] as? WeakReference<T>
        return weakRef?.get()
    }
    
    /**
     * Clear weak reference
     */
    fun clearWeakReference(key: String) {
        weakReferences.remove(key)
    }
    
    /**
     * Clear all weak references
     */
    fun clearAllWeakReferences() {
        weakReferences.clear()
    }
    
    /**
     * Track operation start time
     */
    fun startOperation(operationId: String) {
        activeOperations[operationId] = System.currentTimeMillis()
        Log.d(TAG, "Started operation: $operationId")
    }
    
    /**
     * Track operation end time and log duration
     */
    fun endOperation(operationId: String) {
        val startTime = activeOperations.remove(operationId)
        if (startTime != null) {
            val duration = System.currentTimeMillis() - startTime!!
            Log.d(TAG, "Completed operation: $operationId in ${duration}ms")
        } else {
            Log.w(TAG, "Attempted to end unknown operation: $operationId")
        }
    }
    
    /**
     * Clean up old operations (older than 5 minutes)
     */
    fun cleanupOldOperations() {
        val cutoffTime = System.currentTimeMillis() - (5 * 60 * 1000) // 5 minutes
        val iterator = activeOperations.entries.iterator()
        
        while (iterator.hasNext()) {
            val (id, timestamp) = iterator.next()
            if (timestamp < cutoffTime) {
                iterator.remove()
                Log.d(TAG, "Cleaned up old operation: $id")
            }
        }
    }
    
    /**
     * Get active operation count
     */
    fun getActiveOperationCount(): Int {
        return activeOperations.size
    }
    
    /**
     * Force garbage collection with logging
     */
    fun forceGarbageCollection(reason: String) {
        val beforeMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        System.gc()
        
        // Wait a bit for GC to complete
        Thread.sleep(100)
        
        val afterMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val freedMemory = beforeMemory - afterMemory
        
        Log.d(TAG, "Forced GC ($reason): freed ${freedMemory / 1024}KB")
    }
    
    /**
     * Check for memory leaks and log warnings
     */
    fun checkForMemoryLeaks() {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        val memoryUsagePercent = (usedMemory.toDouble() / totalMemory.toDouble() * 100).toInt()
        
        Log.d(TAG, "Memory usage: ${usedMemory / (1024 * 1024)}MB / ${totalMemory / (1024 * 1024)}MB (${memoryUsagePercent}%)")
        
        // Log warnings for high memory usage
        when {
            memoryUsagePercent > 80 -> {
                Log.w(TAG, "High memory usage detected: ${memoryUsagePercent}%")
            }
            memoryUsagePercent > 90 -> {
                Log.e(TAG, "Critical memory usage: ${memoryUsagePercent}% - consider cleanup")
            }
            getActiveOperationCount() > 10 -> {
                Log.w(TAG, "High number of active operations: ${getActiveOperationCount()}")
            }
        }
        
        // Check for potential memory leaks in weak references
        weakReferences.entries.forEach { (key, weakRef) ->
            if (weakRef.get() == null) {
                Log.d(TAG, "Weak reference for $key has been garbage collected")
            }
        }
    }
    
    /**
     * Create a coroutine scope with proper cleanup
     */
    fun createSafeCoroutineScope(
        name: String,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): CoroutineScope {
        val job = SupervisorJob()
        val scope = CoroutineScope(dispatcher + job)
        
        // Track the scope for cleanup
        storeWeakReference("scope_$name", scope)
        startOperation("scope_$name")
        
        return scope
    }
    
    /**
     * Cancel coroutine scope safely
     */
    fun cancelCoroutineScope(name: String) {
        val scope = getWeakReference<CoroutineScope>("scope_$name", CoroutineScope::class.java)
        scope?.cancel("Scope $name cancelled")
        endOperation("scope_$name")
        clearWeakReference("scope_$name")
    }
    
    /**
     * Create a safe callback that won't leak memory
     */
    fun <T> createSafeCallback(
        name: String,
        callback: (T) -> Unit
    ): (T) -> Unit {
        return { result ->
            try {
                callback(result)
            } catch (e: Exception) {
                Log.e(TAG, "Error in callback $name", e)
            }
        }
    }
    
    /**
     * Monitor memory usage periodically
     */
    fun startMemoryMonitoring(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            while (isActive) {
                delay(30000) // Check every 30 seconds
                
                checkForMemoryLeaks()
                cleanupOldOperations()
                
                // Force GC if memory usage is high
                val runtime = Runtime.getRuntime()
                val memoryUsagePercent = ((runtime.totalMemory() - runtime.freeMemory()).toDouble() / runtime.totalMemory().toDouble() * 100).toInt()
                
                if (memoryUsagePercent > 85) {
                    forceGarbageCollection("High memory usage")
                }
            }
        }
    }
    
    /**
     * Cleanup all resources
     */
    fun cleanup() {
        Log.d(TAG, "Cleaning up all memory leak prevention resources")
        
        clearAllWeakReferences()
        activeOperations.clear()
        
        // Force final garbage collection
        forceGarbageCollection("Final cleanup")
    }
    
    /**
     * Get memory statistics for debugging
     */
    fun getMemoryStats(): Map<String, Any> {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val usedMemory = totalMemory - freeMemory
        
        return mapOf(
            "total_memory_mb" to totalMemory / (1024 * 1024),
            "used_memory_mb" to usedMemory / (1024 * 1024),
            "free_memory_mb" to freeMemory / (1024 * 1024),
            "max_memory_mb" to maxMemory / (1024 * 1024),
            "memory_usage_percent" to (usedMemory.toDouble() / totalMemory.toDouble() * 100).toInt(),
            "active_operations" to getActiveOperationCount(),
            "weak_references_count" to weakReferences.size,
            "available_processors" to runtime.availableProcessors()
        )
    }
}
