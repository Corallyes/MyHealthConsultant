package com.example.myhealthconsultant.data.repository

import com.example.myhealthconsultant.data.local.dao.ScanHistoryDao
import com.example.myhealthconsultant.data.local.entity.ScanHistory
import com.example.myhealthconsultant.domain.repository.ScanHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ScanHistoryRepositoryImpl @Inject constructor(
    private val dao: ScanHistoryDao
) : ScanHistoryRepository {

    override fun getAllScans(userId: String): Flow<List<ScanHistory>> = dao.getAllScans(userId)

    override fun getRecentScans(userId: String, limit: Int): Flow<List<ScanHistory>> = 
        dao.getRecentScans(userId, limit)

    override suspend fun insertScan(scan: ScanHistory) = dao.insertScan(scan)

    override suspend fun deleteScan(scan: ScanHistory) = dao.deleteScan(scan)

    override suspend fun deleteAllScans(userId: String) = dao.deleteAllScans(userId)
}
