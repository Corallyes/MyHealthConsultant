package com.example.myhealthconsultant.data.repository

import com.example.myhealthconsultant.data.local.dao.CabinetMedicineDao
import com.example.myhealthconsultant.data.local.entity.CabinetMedicine
import com.example.myhealthconsultant.domain.repository.CabinetMedicineRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CabinetMedicineRepositoryImpl @Inject constructor(
    private val dao: CabinetMedicineDao
) : CabinetMedicineRepository {

    override fun getAll(userId: String): Flow<List<CabinetMedicine>> = dao.getAll(userId)

    override fun search(userId: String, query: String): Flow<List<CabinetMedicine>> = dao.search(userId, query)

    override fun getById(id: String): Flow<CabinetMedicine?> = dao.getById(id)

    override fun getExpiringSoon(userId: String, timestamp: Long): Flow<List<CabinetMedicine>> = dao.getExpiringSoon(userId, timestamp)

    override fun getExpired(userId: String, timestamp: Long): Flow<List<CabinetMedicine>> = dao.getExpired(userId, timestamp)

    override fun getByPrescriptionId(prescriptionId: String): Flow<List<CabinetMedicine>> = dao.getByPrescriptionId(prescriptionId)

    override suspend fun insert(medicine: CabinetMedicine) = dao.insert(medicine)

    override suspend fun update(medicine: CabinetMedicine) = dao.update(medicine)

    override suspend fun delete(medicine: CabinetMedicine) = dao.delete(medicine)

    override suspend fun deactivate(id: String) = dao.deactivate(id)
}
