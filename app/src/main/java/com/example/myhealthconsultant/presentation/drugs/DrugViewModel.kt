package com.example.myhealthconsultant.presentation.drugs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhealthconsultant.data.local.entity.Drug
import com.example.myhealthconsultant.domain.repository.DrugRepository
import com.example.myhealthconsultant.util.FuzzySearchUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DrugUiState(
    val drugs: List<Drug> = emptyList(),
    val filteredDrugs: List<Drug> = emptyList(),
    val recommendedDrugs: List<Drug> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val showOnlyFavorites: Boolean = false,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DrugViewModel @Inject constructor(
    private val drugRepository: DrugRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DrugUiState())
    val uiState: StateFlow<DrugUiState> = _uiState.asStateFlow()

    init {
        loadDrugs()
        loadCategories()
    }

    private fun loadDrugs() {
        viewModelScope.launch {
            drugRepository.getAllDrugs().collect { drugs ->
                _uiState.update {
                    it.copy(
                        drugs = drugs,
                        filteredDrugs = filterDrugs(drugs, it.searchQuery, it.selectedCategory, it.showOnlyFavorites),
                        recommendedDrugs = drugs.shuffled().take(8)
                    )
                }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            drugRepository.getAllCategories().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredDrugs = filterDrugs(state.drugs, query, state.selectedCategory, state.showOnlyFavorites)
            )
        }
    }

    fun onCategorySelected(category: String?) {
        _uiState.update { state ->
            state.copy(
                selectedCategory = category,
                showOnlyFavorites = false,
                filteredDrugs = filterDrugs(state.drugs, state.searchQuery, category, false)
            )
        }
    }

    fun onFavoritesSelected() {
        _uiState.update { state ->
            state.copy(
                selectedCategory = null,
                showOnlyFavorites = true,
                filteredDrugs = filterDrugs(state.drugs, state.searchQuery, null, true)
            )
        }
    }

    fun toggleFavorite(drugId: String) {
        viewModelScope.launch {
            val drug = _uiState.value.drugs.find { it.id == drugId } ?: return@launch
            drugRepository.toggleFavorite(drugId, !drug.isFavorite)
        }
    }

    fun refreshRecommendations() {
        _uiState.update { state ->
            state.copy(recommendedDrugs = state.drugs.shuffled().take(8))
        }
    }

    private fun filterDrugs(
        drugs: List<Drug>,
        query: String,
        category: String?,
        showOnlyFavorites: Boolean = false
    ): List<Drug> {
        // 先按收藏筛选
        val favoritesFiltered = if (showOnlyFavorites) {
            drugs.filter { it.isFavorite }
        } else {
            drugs
        }

        // 再按分类筛选
        val categoryFiltered = if (category != null) {
            favoritesFiltered.filter { it.category == category }
        } else {
            favoritesFiltered
        }

        // 如果没有搜索词，返回全部
        if (query.isEmpty()) return categoryFiltered

        // 使用模糊搜索
        val fuzzyResults = FuzzySearchUtil.fuzzyFilter(
            query = query,
            items = categoryFiltered,
            selectors = arrayOf(
                { it.name },
                { it.genericName },
                { it.category },
                { it.indications },
                { it.ingredients }
            ),
            threshold = 0.5  // 相似度阈值
        )

        return fuzzyResults.map { it.first }
    }
}
