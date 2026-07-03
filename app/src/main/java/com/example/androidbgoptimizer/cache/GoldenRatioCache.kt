package com.example.androidbgoptimizer.cache

import kotlin.math.floor

/**
 * Golden Ratio Cache Eviction Engine
 * φ = 1.618033988749895...
 * 
 * Partitions cache into HOT (61.8%) and COLD (38.2%) segments.
 * New processes enter COLD. Second access promotes to HOT.
 * When full, LRU items are demoted/evicted.
 */

const val PHI = 1.618033988749895

data class CacheProcess(
    val id: String,
    val name: String,
    val icon: String,
    val memoryMB: Int,
    val segment: String, // "HOT" or "COLD"
    var lastAccessed: Long = System.currentTimeMillis(),
    var accessCount: Int = 0,
    var createdAt: Long = System.currentTimeMillis()
)

data class CacheStats(
    val hitRate: Double = 0.0,
    val cacheHits: Int = 0,
    val cacheMisses: Int = 0,
    val hotCount: Int = 0,
    val coldCount: Int = 0,
    val totalCount: Int = 0,
    val hotUsedMB: Double = 0.0,
    val coldUsedMB: Double = 0.0,
    val totalUsedMB: Double = 0.0,
    val memoryPressure: Double = 0.0,
    val phiRatio: Double = 0.0,
    val promotions: Int = 0,
    val demotions: Int = 0,
    val evictedCount: Int = 0
)

data class CacheEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: String, // ADMIT, HIT_HOT, HIT_COLD, PROMOTE, DEMOTE, EVICT, etc.
    val processName: String,
    val detail: String,
    val timestamp: Long = System.currentTimeMillis()
)

class GoldenRatioCache(val totalCapacityMB: Int = 2048) {
    private val hotSegment = mutableListOf<CacheProcess>()
    private val coldSegment = mutableListOf<CacheProcess>()
    private val evictedProcesses = mutableListOf<CacheProcess>()
    private val eventLog = mutableListOf<CacheEvent>()

    private var cacheHits = 0
    private var cacheMisses = 0
    private var promotions = 0
    private var demotions = 0
    private var evictedCount = 0

    fun computeCapacities(): Pair<Int, Int> {
        val hotMB = floor(totalCapacityMB / PHI).toInt()
        val coldMB = totalCapacityMB - hotMB
        return Pair(hotMB, coldMB)
    }

    fun admitProcess(process: CacheProcess): Boolean {
        val (_, coldCapacity) = computeCapacities()
        val coldUsed = coldSegment.sumOf { it.memoryMB }

        // If process already cached, just touch it
        val existing = hotSegment.find { it.id == process.id } ?: coldSegment.find { it.id == process.id }
        if (existing != null) {
            touchProcess(process.id)
            return true
        }

        // Try to fit in COLD
        if (coldUsed + process.memoryMB <= coldCapacity) {
            val newProc = process.copy(segment = "COLD", lastAccessed = System.currentTimeMillis())
            coldSegment.add(newProc)
            logEvent(CacheEvent(
                type = "ADMIT",
                processName = process.name,
                detail = "Admitted to COLD (${process.memoryMB} MB)"
            ))
            return true
        }

        // COLD is full, evict LRU from COLD
        if (coldSegment.isNotEmpty()) {
            val lruCold = coldSegment.minByOrNull { it.lastAccessed }!!
            coldSegment.remove(lruCold)
            evictedProcesses.add(lruCold)
            evictedCount++
            logEvent(CacheEvent(
                type = "EVICT",
                processName = lruCold.name,
                detail = "Evicted from COLD (freed ${lruCold.memoryMB} MB)"
            ))
        }

        // Retry admission
        val newProc = process.copy(segment = "COLD", lastAccessed = System.currentTimeMillis())
        coldSegment.add(newProc)
        logEvent(CacheEvent(
            type = "ADMIT",
            processName = process.name,
            detail = "Admitted to COLD after eviction"
        ))
        return true
    }

    fun touchProcess(processId: String): Boolean {
        val process = hotSegment.find { it.id == processId }
        if (process != null) {
            process.lastAccessed = System.currentTimeMillis()
            process.accessCount++
            cacheHits++
            logEvent(CacheEvent(
                type = "HIT_HOT",
                processName = process.name,
                detail = "Cache hit in HOT (×${process.accessCount})"
            ))
            return true
        }

        val coldProc = coldSegment.find { it.id == processId }
        if (coldProc != null) {
            coldProc.lastAccessed = System.currentTimeMillis()
            coldProc.accessCount++
            cacheHits++
            logEvent(CacheEvent(
                type = "HIT_COLD",
                processName = coldProc.name,
                detail = "Cache hit in COLD, promoting to HOT"
            ))

            // Promote to HOT
            coldSegment.remove(coldProc)
            promoteToHot(coldProc)
            return true
        }

        cacheMisses++
        return false
    }

    private fun promoteToHot(process: CacheProcess) {
        val (hotCapacity, _) = computeCapacities()
        val hotUsed = hotSegment.sumOf { it.memoryMB }

        val promotedProc = process.copy(segment = "HOT")

        if (hotUsed + process.memoryMB <= hotCapacity) {
            hotSegment.add(promotedProc)
            promotions++
            logEvent(CacheEvent(
                type = "PROMOTE",
                processName = process.name,
                detail = "Promoted COLD → HOT (${process.memoryMB} MB)"
            ))
        } else {
            // HOT is full, demote LRU from HOT to COLD
            val lruHot = hotSegment.minByOrNull { it.lastAccessed }!!
            hotSegment.remove(lruHot)
            val demotedProc = lruHot.copy(segment = "COLD")
            coldSegment.add(demotedProc)
            demotions++
            logEvent(CacheEvent(
                type = "DEMOTE",
                processName = lruHot.name,
                detail = "Demoted HOT → COLD (freed ${lruHot.memoryMB} MB)"
            ))

            // Now add the promoted process to HOT
            hotSegment.add(promotedProc)
            promotions++
            logEvent(CacheEvent(
                type = "PROMOTE",
                processName = process.name,
                detail = "Promoted COLD → HOT after demotion"
            ))
        }
    }

    fun evictProcess(processId: String): Boolean {
        val hotProc = hotSegment.find { it.id == processId }
        if (hotProc != null) {
            hotSegment.remove(hotProc)
            evictedProcesses.add(hotProc)
            evictedCount++
            logEvent(CacheEvent(
                type = "MANUAL_EVICT",
                processName = hotProc.name,
                detail = "Manually evicted from HOT (freed ${hotProc.memoryMB} MB)"
            ))
            return true
        }

        val coldProc = coldSegment.find { it.id == processId }
        if (coldProc != null) {
            coldSegment.remove(coldProc)
            evictedProcesses.add(coldProc)
            evictedCount++
            logEvent(CacheEvent(
                type = "MANUAL_EVICT",
                processName = coldProc.name,
                detail = "Manually evicted from COLD (freed ${coldProc.memoryMB} MB)"
            ))
            return true
        }

        return false
    }

    fun getStats(): CacheStats {
        val (hotCapacity, coldCapacity) = computeCapacities()
        val hotUsed = hotSegment.sumOf { it.memoryMB }.toDouble()
        val coldUsed = coldSegment.sumOf { it.memoryMB }.toDouble()
        val totalUsed = hotUsed + coldUsed
        val totalAccesses = cacheHits + cacheMisses
        val hitRate = if (totalAccesses > 0) cacheHits.toDouble() / totalAccesses else 0.0
        val memoryPressure = totalUsed / totalCapacityMB
        val phiRatio = if (coldUsed > 0) hotUsed / coldUsed else 0.0

        return CacheStats(
            hitRate = hitRate,
            cacheHits = cacheHits,
            cacheMisses = cacheMisses,
            hotCount = hotSegment.size,
            coldCount = coldSegment.size,
            totalCount = hotSegment.size + coldSegment.size,
            hotUsedMB = hotUsed,
            coldUsedMB = coldUsed,
            totalUsedMB = totalUsed,
            memoryPressure = memoryPressure,
            phiRatio = phiRatio,
            promotions = promotions,
            demotions = demotions,
            evictedCount = evictedCount
        )
    }

    fun getHotProcesses(): List<CacheProcess> = hotSegment.sortedByDescending { it.lastAccessed }
    fun getColdProcesses(): List<CacheProcess> = coldSegment.sortedByDescending { it.lastAccessed }
    fun getAllProcesses(): List<CacheProcess> = (hotSegment + coldSegment).sortedByDescending { it.lastAccessed }
    fun getEvictedProcesses(): List<CacheProcess> = evictedProcesses.takeLast(20)
    fun getEventLog(): List<CacheEvent> = eventLog.takeLast(100)

    private fun logEvent(event: CacheEvent) {
        eventLog.add(event)
    }

    fun clearHistory() {
        eventLog.clear()
    }
}
