package com.example.myhealthconsultant.data.repository

import com.example.myhealthconsultant.data.local.dao.DrugDao
import com.example.myhealthconsultant.data.local.entity.Drug
import com.example.myhealthconsultant.domain.repository.DrugRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DrugRepositoryImpl @Inject constructor(
    private val drugDao: DrugDao
) : DrugRepository {

    override fun getAllDrugs(): Flow<List<Drug>> = drugDao.getAllDrugs()

    override fun getDrugById(drugId: String): Flow<Drug?> = drugDao.getDrugById(drugId)

    override fun searchDrugs(query: String): Flow<List<Drug>> = drugDao.searchDrugs(query)

    override fun getDrugsByCategory(category: String): Flow<List<Drug>> = drugDao.getDrugsByCategory(category)

    override fun getAllCategories(): Flow<List<String>> = drugDao.getAllCategories()

    override fun getFavoriteDrugs(): Flow<List<Drug>> = drugDao.getFavoriteDrugs()

    override suspend fun insertDrug(drug: Drug) = drugDao.insertDrug(drug)

    override suspend fun insertDrugs(drugs: List<Drug>) = drugDao.insertDrugs(drugs)

    override suspend fun updateDrug(drug: Drug) = drugDao.updateDrug(drug)

    override suspend fun deleteDrug(drug: Drug) = drugDao.deleteDrug(drug)

    override suspend fun toggleFavorite(drugId: String, isFavorite: Boolean) = drugDao.toggleFavorite(drugId, isFavorite)

    override suspend fun getDrugCount(): Int = drugDao.getDrugCount()
}
