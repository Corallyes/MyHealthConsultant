package com.example.myhealthconsultant.data.local.dao

import androidx.room.*
import com.example.myhealthconsultant.data.local.entity.Prescription
import kotlinx.coroutines.flow.Flow

@Dao
interface PrescriptionDao {
    @Query("SELECT * FROM prescriptions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAll(userId: String): Flow<List<Prescription>>

    @Query("SELECT * FROM prescriptions WHERE id = :id")
    fun getById(id: String): Flow<Prescription?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prescription: Prescription)

    @Update
    suspend fun update(prescription: Prescription)

    @Delete
    suspend fun delete(prescription: Prescription)
}
