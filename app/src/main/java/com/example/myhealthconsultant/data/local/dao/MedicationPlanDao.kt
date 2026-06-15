package com.example.myhealthconsultant.data.local.dao

import androidx.room.*
import com.example.myhealthconsultant.data.local.entity.MedicationPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationPlanDao {
    @Query("SELECT * FROM medication_plans WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC")
    fun getActivePlans(userId: String): Flow<List<MedicationPlan>>

    @Query("SELECT * FROM medication_plans WHERE userId = :userId AND timeSlot = :timeSlot AND isActive = 1")
    fun getPlansByTimeSlot(userId: String, timeSlot: String): Flow<List<MedicationPlan>>

    @Query("SELECT * FROM medication_plans WHERE id = :planId")
    fun getPlanById(planId: String): Flow<MedicationPlan?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: MedicationPlan)

    @Update
    suspend fun updatePlan(plan: MedicationPlan)

    @Query("UPDATE medication_plans SET isActive = 0 WHERE id = :planId")
    suspend fun deactivatePlan(planId: String)

    @Delete
    suspend fun deletePlan(plan: MedicationPlan)
}
