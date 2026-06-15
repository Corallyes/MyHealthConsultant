package com.example.myhealthconsultant.data.repository

import com.example.myhealthconsultant.data.local.dao.MedicationPlanDao
import com.example.myhealthconsultant.data.local.entity.MedicationPlan
import com.example.myhealthconsultant.domain.repository.MedicationPlanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MedicationPlanRepositoryImpl @Inject constructor(
    private val dao: MedicationPlanDao
) : MedicationPlanRepository {

    override fun getActivePlans(userId: String): Flow<List<MedicationPlan>> = dao.getActivePlans(userId)

    override fun getPlansByTimeSlot(userId: String, timeSlot: String): Flow<List<MedicationPlan>> = 
        dao.getPlansByTimeSlot(userId, timeSlot)

    override fun getPlanById(planId: String): Flow<MedicationPlan?> = dao.getPlanById(planId)

    override suspend fun insertPlan(plan: MedicationPlan) = dao.insertPlan(plan)

    override suspend fun updatePlan(plan: MedicationPlan) = dao.updatePlan(plan)

    override suspend fun deactivatePlan(planId: String) = dao.deactivatePlan(planId)

    override suspend fun deletePlan(plan: MedicationPlan) = dao.deletePlan(plan)
}
