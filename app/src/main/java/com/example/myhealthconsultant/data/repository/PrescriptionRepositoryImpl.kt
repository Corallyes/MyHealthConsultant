package com.example.myhealthconsultant.data.repository

import com.example.myhealthconsultant.data.local.dao.PrescriptionDao
import com.example.myhealthconsultant.data.local.entity.Prescription
import com.example.myhealthconsultant.domain.repository.PrescriptionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PrescriptionRepositoryImpl @Inject constructor(
    private val dao: PrescriptionDao
) : PrescriptionRepository {

    override fun getAll(userId: String): Flow<List<Prescription>> = dao.getAll(userId)

    override fun getById(id: String): Flow<Prescription?> = dao.getById(id)

    override suspend fun insert(prescription: Prescription) = dao.insert(prescription)

    override suspend fun update(prescription: Prescription) = dao.update(prescription)

    override suspend fun delete(prescription: Prescription) = dao.delete(prescription)
}
