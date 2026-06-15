package com.example.myhealthconsultant.data.local.dao

import androidx.room.*
import com.example.myhealthconsultant.data.local.entity.Drug
import kotlinx.coroutines.flow.Flow

@Dao
interface DrugDao {
    @Query("SELECT * FROM drugs ORDER BY name ASC")
    fun getAllDrugs(): Flow<List<Drug>>

    @Query("SELECT * FROM drugs WHERE id = :drugId")
    fun getDrugById(drugId: String): Flow<Drug?>

    @Query("SELECT * FROM drugs WHERE name LIKE '%' || :query || '%' OR genericName LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR indications LIKE '%' || :query || '%' OR ingredients LIKE '%' || :query || '%'")
    fun searchDrugs(query: String): Flow<List<Drug>>

    @Query("SELECT * FROM drugs WHERE category = :category ORDER BY name ASC")
    fun getDrugsByCategory(category: String): Flow<List<Drug>>

    @Query("SELECT DISTINCT category FROM drugs ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM drugs WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteDrugs(): Flow<List<Drug>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrug(drug: Drug)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrugs(drugs: List<Drug>)

    @Update
    suspend fun updateDrug(drug: Drug)

    @Delete
    suspend fun deleteDrug(drug: Drug)

    @Query("UPDATE drugs SET isFavorite = :isFavorite WHERE id = :drugId")
    suspend fun toggleFavorite(drugId: String, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM drugs")
    suspend fun getDrugCount(): Int
}
