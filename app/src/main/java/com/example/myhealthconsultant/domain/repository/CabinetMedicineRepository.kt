package com.example.myhealthconsultant.domain.repository

import com.example.myhealthconsultant.data.local.entity.CabinetMedicine
import kotlinx.coroutines.flow.Flow

interface CabinetMedicineRepository {
    fun getAll(userId: String): Flow<List<CabinetMedicine>>
    fun search(userId: String, query: String): Flow<List<CabinetMedicine>>
    fun getById(id: String): Flow<CabinetMedicine?>
    fun getExpiringSoon(userId: String, timestamp: Long): Flow<List<CabinetMedicine>>
    fun getExpired(userId: String, timestamp: Long): Flow<List<CabinetMedicine>>
    fun getByPrescriptionId(prescriptionId: String): Flow<List<CabinetMedicine>>
    suspend fun insert(medicine: CabinetMedicine)
    suspend fun update(medicine: CabinetMedicine)
    suspend fun delete(medicine: CabinetMedicine)
    suspend fun deactivate(id: String)
}
