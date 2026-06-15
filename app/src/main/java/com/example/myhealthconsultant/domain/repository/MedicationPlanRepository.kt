package com.example.myhealthconsultant.domain.repository

import com.example.myhealthconsultant.data.local.entity.MedicationPlan
import kotlinx.coroutines.flow.Flow

interface MedicationPlanRepository {
    fun getActivePlans(userId: String): Flow<List<MedicationPlan>>
    fun getPlansByTimeSlot(userId: String, timeSlot: String): Flow<List<MedicationPlan>>
    fun getPlanById(planId: String): Flow<MedicationPlan?>
    suspend fun insertPlan(plan: MedicationPlan)
    suspend fun updatePlan(plan: MedicationPlan)
    suspend fun deactivatePlan(planId: String)
    suspend fun deletePlan(plan: MedicationPlan)
}
