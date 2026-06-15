package com.example.myhealthconsultant.domain.repository

import com.example.myhealthconsultant.data.local.entity.Prescription
import kotlinx.coroutines.flow.Flow

interface PrescriptionRepository {
    fun getAll(userId: String): Flow<List<Prescription>>
    fun getById(id: String): Flow<Prescription?>
    suspend fun insert(prescription: Prescription)
    suspend fun update(prescription: Prescription)
    suspend fun delete(prescription: Prescription)
}
