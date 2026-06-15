package com.example.myhealthconsultant.data.repository

import com.example.myhealthconsultant.data.local.dao.MedicationRecordDao
import com.example.myhealthconsultant.data.local.entity.MedicationRecord
import com.example.myhealthconsultant.domain.repository.MedicationRecordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MedicationRecordRepositoryImpl @Inject constructor(
    private val dao: MedicationRecordDao
) : MedicationRecordRepository {

    override fun getRecordsByDate(userId: String, date: Long): Flow<List<MedicationRecord>> = 
        dao.getRecordsByDate(userId, date)

    override fun getRecordsByPlan(planId: String): Flow<List<MedicationRecord>> = 
        dao.getRecordsByPlan(planId)

    override suspend fun getRecordByPlanAndDate(userId: String, planId: String, date: Long): MedicationRecord? = 
        dao.getRecordByPlanAndDate(userId, planId, date)

    override fun getRecentRecords(userId: String, limit: Int): Flow<List<MedicationRecord>> = 
        dao.getRecentRecords(userId, limit)

    override suspend fun insertRecord(record: MedicationRecord) = dao.insertRecord(record)

    override suspend fun deleteRecord(record: MedicationRecord) = dao.deleteRecord(record)

    override suspend fun deleteAllRecords(userId: String) = dao.deleteAllRecords(userId)
}
