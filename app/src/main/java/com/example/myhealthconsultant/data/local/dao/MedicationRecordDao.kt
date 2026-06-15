package com.example.myhealthconsultant.data.local.dao

import androidx.room.*
import com.example.myhealthconsultant.data.local.entity.MedicationRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationRecordDao {
    @Query("SELECT * FROM medication_records WHERE userId = :userId AND takenDate = :date ORDER BY takenTime ASC")
    fun getRecordsByDate(userId: String, date: Long): Flow<List<MedicationRecord>>

    @Query("SELECT * FROM medication_records WHERE planId = :planId ORDER BY takenDate DESC")
    fun getRecordsByPlan(planId: String): Flow<List<MedicationRecord>>

    @Query("SELECT * FROM medication_records WHERE userId = :userId AND planId = :planId AND takenDate = :date LIMIT 1")
    suspend fun getRecordByPlanAndDate(userId: String, planId: String, date: Long): MedicationRecord?

    @Query("SELECT * FROM medication_records WHERE userId = :userId ORDER BY takenDate DESC LIMIT :limit")
    fun getRecentRecords(userId: String, limit: Int = 30): Flow<List<MedicationRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: MedicationRecord)

    @Delete
    suspend fun deleteRecord(record: MedicationRecord)

    @Query("DELETE FROM medication_records WHERE userId = :userId")
    suspend fun deleteAllRecords(userId: String)
}
