package com.example.myhealthconsultant.domain.repository

import com.example.myhealthconsultant.data.local.entity.MedicationRecord
import kotlinx.coroutines.flow.Flow

interface MedicationRecordRepository {
    fun getRecordsByDate(userId: String, date: Long): Flow<List<MedicationRecord>>
    fun getRecordsByPlan(planId: String): Flow<List<MedicationRecord>>
    suspend fun getRecordByPlanAndDate(userId: String, planId: String, date: Long): MedicationRecord?
    fun getRecentRecords(userId: String, limit: Int = 30): Flow<List<MedicationRecord>>
    suspend fun insertRecord(record: MedicationRecord)
    suspend fun deleteRecord(record: MedicationRecord)
    suspend fun deleteAllRecords(userId: String)
}
