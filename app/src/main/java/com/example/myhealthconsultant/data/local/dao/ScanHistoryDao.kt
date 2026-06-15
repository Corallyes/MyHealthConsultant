package com.example.myhealthconsultant.data.local.dao

import androidx.room.*
import com.example.myhealthconsultant.data.local.entity.ScanHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {
    @Query("SELECT * FROM scan_history WHERE userId = :userId ORDER BY scannedAt DESC")
    fun getAllScans(userId: String): Flow<List<ScanHistory>>

    @Query("SELECT * FROM scan_history WHERE userId = :userId ORDER BY scannedAt DESC LIMIT :limit")
    fun getRecentScans(userId: String, limit: Int = 20): Flow<List<ScanHistory>>

    @Insert
    suspend fun insertScan(scan: ScanHistory)

    @Delete
    suspend fun deleteScan(scan: ScanHistory)

    @Query("DELETE FROM scan_history WHERE userId = :userId")
    suspend fun deleteAllScans(userId: String)
}
