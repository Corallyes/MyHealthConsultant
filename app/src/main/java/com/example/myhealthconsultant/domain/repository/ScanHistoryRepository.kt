package com.example.myhealthconsultant.domain.repository

import com.example.myhealthconsultant.data.local.entity.ScanHistory
import kotlinx.coroutines.flow.Flow

interface ScanHistoryRepository {
    fun getAllScans(userId: String): Flow<List<ScanHistory>>
    fun getRecentScans(userId: String, limit: Int = 20): Flow<List<ScanHistory>>
    suspend fun insertScan(scan: ScanHistory)
    suspend fun deleteScan(scan: ScanHistory)
    suspend fun deleteAllScans(userId: String)
}
