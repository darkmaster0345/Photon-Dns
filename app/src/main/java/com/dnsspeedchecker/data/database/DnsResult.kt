package com.dnsspeedchecker.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dns_results")
data class DnsResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val serverId: String,
    val serverName: String,
    val serverIp: String,
    val latencyMs: Long,
    val timestamp: Long,
    val success: Boolean
)
