package com.example.myhealthconsultant.domain.repository

import com.example.myhealthconsultant.data.local.entity.Drug
import kotlinx.coroutines.flow.Flow

interface DrugRepository {
    fun getAllDrugs(): Flow<List<Drug>>
    fun getDrugById(drugId: String): Flow<Drug?>
    fun searchDrugs(query: String): Flow<List<Drug>>
    fun getDrugsByCategory(category: String): Flow<List<Drug>>
    fun getAllCategories(): Flow<List<String>>
    fun getFavoriteDrugs(): Flow<List<Drug>>
    suspend fun insertDrug(drug: Drug)
    suspend fun insertDrugs(drugs: List<Drug>)
    suspend fun updateDrug(drug: Drug)
    suspend fun deleteDrug(drug: Drug)
    suspend fun toggleFavorite(drugId: String, isFavorite: Boolean)
    suspend fun getDrugCount(): Int
}
