package com.example.myhealthconsultant.data.local.dao

import androidx.room.*
import com.example.myhealthconsultant.data.local.entity.CabinetMedicine
import kotlinx.coroutines.flow.Flow

@Dao
interface CabinetMedicineDao {
    @Query("SELECT * FROM cabinet_medicines WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC")
    fun getAll(userId: String): Flow<List<CabinetMedicine>>

    @Query("SELECT * FROM cabinet_medicines WHERE userId = :userId AND isActive = 1 AND (name LIKE '%' || :query || '%' OR genericName LIKE '%' || :query || '%')")
    fun search(userId: String, query: String): Flow<List<CabinetMedicine>>

    @Query("SELECT * FROM cabinet_medicines WHERE id = :id")
    fun getById(id: String): Flow<CabinetMedicine?>

    @Query("SELECT * FROM cabinet_medicines WHERE userId = :userId AND isActive = 1 AND expiryDate IS NOT NULL AND expiryDate <= :timestamp")
    fun getExpiringSoon(userId: String, timestamp: Long): Flow<List<CabinetMedicine>>

    @Query("SELECT * FROM cabinet_medicines WHERE userId = :userId AND isActive = 1 AND expiryDate IS NOT NULL AND expiryDate < :timestamp")
    fun getExpired(userId: String, timestamp: Long): Flow<List<CabinetMedicine>>

    @Query("SELECT * FROM cabinet_medicines WHERE prescriptionId = :prescriptionId AND isActive = 1")
    fun getByPrescriptionId(prescriptionId: String): Flow<List<CabinetMedicine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medicine: CabinetMedicine)

    @Update
    suspend fun update(medicine: CabinetMedicine)

    @Delete
    suspend fun delete(medicine: CabinetMedicine)

    @Query("UPDATE cabinet_medicines SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: String)

    @Query("SELECT * FROM cabinet_medicines WHERE userId = :userId AND isActive = 1 AND expiryDate IS NOT NULL AND expiryDate <= :timestamp")
    suspend fun getExpiringSoonSync(userId: String, timestamp: Long): List<CabinetMedicine>

    @Query("SELECT * FROM cabinet_medicines WHERE userId = :userId AND isActive = 1 AND expiryDate IS NOT NULL AND expiryDate < :timestamp")
    suspend fun getExpiredSync(userId: String, timestamp: Long): List<CabinetMedicine>
}
