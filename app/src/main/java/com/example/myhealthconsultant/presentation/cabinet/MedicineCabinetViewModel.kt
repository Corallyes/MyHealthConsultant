package com.example.myhealthconsultant.presentation.cabinet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhealthconsultant.data.local.entity.CabinetMedicine
import com.example.myhealthconsultant.data.local.entity.Prescription
import com.example.myhealthconsultant.domain.repository.CabinetMedicineRepository
import com.example.myhealthconsultant.domain.repository.PrescriptionRepository
import com.example.myhealthconsultant.util.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortType(val label: String) {
    BY_EXPIRY("按有效期"),
    BY_NAME("按名称"),
    BY_DATE_ADDED("按添加时间")
}

data class CabinetUiState(
    val medicines: List<CabinetMedicine> = emptyList(),
    val filteredMedicines: List<CabinetMedicine> = emptyList(),
    val prescriptions: List<Prescription> = emptyList(),
    val searchQuery: String = "",
    val sortType: SortType = SortType.BY_EXPIRY,
    val expiryThresholdDays: Int = 7,
    val totalCount: Int = 0,
    val expiringSoonCount: Int = 0,
    val expiredCount: Int = 0,
    val isLoading: Boolean = false,
    val message: String? = null,
    val fabPosition: Pair<Float, Float> = Pair(Float.NaN, Float.NaN)
)

@HiltViewModel
class MedicineCabinetViewModel @Inject constructor(
    private val medicineRepository: CabinetMedicineRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CabinetUiState())
    val uiState: StateFlow<CabinetUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dataStoreManager.expiryThresholdDays.collect { days ->
                _uiState.update { it.copy(expiryThresholdDays = days) }
            }
        }
        loadMedicines()
        loadPrescriptions()
        loadFabPosition()
    }

    private fun loadMedicines() {
        viewModelScope.launch {
            val userId = dataStoreManager.getLoggedInUserId() ?: return@launch
            medicineRepository.getAll(userId).collect { medicines ->
                refreshList(medicines)
            }
        }
    }

    private fun refreshList(medicines: List<CabinetMedicine>) {
        val state = _uiState.value
        val now = System.currentTimeMillis()
        val thresholdMs = state.expiryThresholdDays.toLong() * 24 * 60 * 60 * 1000
        val thresholdDate = now + thresholdMs
        val expiringSoon = medicines.filter { m ->
            m.expiryDate != null && m.expiryDate in (now + 1)..thresholdDate
        }
        val expired = medicines.filter { m ->
            m.expiryDate != null && m.expiryDate < now
        }
        val filtered = if (state.searchQuery.isEmpty()) medicines
            else medicines.filter { m ->
                m.name.contains(state.searchQuery, true) || m.genericName?.contains(state.searchQuery, true) == true
            }
        val sorted = sortMedicines(filtered, state.sortType)
        _uiState.update {
            it.copy(
                medicines = medicines,
                filteredMedicines = sorted,
                totalCount = medicines.size,
                expiringSoonCount = expiringSoon.size,
                expiredCount = expired.size
            )
        }
    }

    private fun sortMedicines(medicines: List<CabinetMedicine>, sortType: SortType): List<CabinetMedicine> {
        return when (sortType) {
            SortType.BY_EXPIRY -> medicines.sortedBy { it.expiryDate ?: Long.MAX_VALUE }
            SortType.BY_NAME -> medicines.sortedBy { it.name }
            SortType.BY_DATE_ADDED -> medicines.sortedByDescending { it.createdAt }
        }
    }

    private fun loadPrescriptions() {
        viewModelScope.launch {
            val userId = dataStoreManager.getLoggedInUserId() ?: return@launch
            prescriptionRepository.getAll(userId).collect { prescriptions ->
                _uiState.update { it.copy(prescriptions = prescriptions) }
            }
        }
    }

    fun search(query: String) {
        _uiState.update { state ->
            val filtered = if (query.isEmpty()) state.medicines
                else state.medicines.filter { m ->
                    m.name.contains(query, true) || m.genericName?.contains(query, true) == true
                }
            state.copy(
                searchQuery = query,
                filteredMedicines = sortMedicines(filtered, state.sortType)
            )
        }
    }

    fun setSortType(sortType: SortType) {
        _uiState.update { state ->
            state.copy(
                sortType = sortType,
                filteredMedicines = sortMedicines(state.filteredMedicines, sortType)
            )
        }
    }

    fun setExpiryThresholdDays(days: Int) {
        viewModelScope.launch {
            dataStoreManager.setExpiryThresholdDays(days)
            val medicines = _uiState.value.medicines
            _uiState.update { it.copy(expiryThresholdDays = days) }
            refreshList(medicines)
        }
    }

    fun addMedicine(
        name: String,
        genericName: String?,
        category: String,
        specification: String,
        quantity: Int,
        unit: String,
        expiryDate: Long?,
        storageLocation: String,
        notes: String
    ) {
        viewModelScope.launch {
            try {
                val userId = dataStoreManager.getLoggedInUserId() ?: return@launch
                val medicine = CabinetMedicine(
                    userId = userId,
                    name = name,
                    genericName = genericName,
                    category = category,
                    specification = specification,
                    quantity = quantity,
                    unit = unit,
                    expiryDate = expiryDate,
                    storageLocation = storageLocation,
                    notes = notes
                )
                medicineRepository.insert(medicine)
                _uiState.update { it.copy(message = "添加成功") }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "添加失败: ${e.message}") }
            }
        }
    }

    fun updateMedicine(medicine: CabinetMedicine) {
        viewModelScope.launch {
            try {
                medicineRepository.update(medicine)
                _uiState.update { it.copy(message = "更新成功") }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "更新失败: ${e.message}") }
            }
        }
    }

    fun deleteMedicine(medicine: CabinetMedicine) {
        viewModelScope.launch {
            try {
                medicineRepository.deactivate(medicine.id)
                _uiState.update { it.copy(message = "已移除") }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "移除失败: ${e.message}") }
            }
        }
    }

    fun addPrescription(
        doctorName: String,
        hospitalName: String,
        diagnosis: String,
        notes: String
    ) {
        viewModelScope.launch {
            try {
                val userId = dataStoreManager.getLoggedInUserId() ?: return@launch
                val prescription = Prescription(
                    userId = userId,
                    doctorName = doctorName,
                    hospitalName = hospitalName,
                    diagnosis = diagnosis,
                    notes = notes
                )
                prescriptionRepository.insert(prescription)
                _uiState.update { it.copy(message = "医嘱添加成功") }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "添加失败: ${e.message}") }
            }
        }
    }

    fun linkPrescription(medicineId: String, prescriptionId: String?) {
        viewModelScope.launch {
            try {
                medicineRepository.getById(medicineId).firstOrNull()?.let { medicine ->
                    medicineRepository.update(medicine.copy(prescriptionId = prescriptionId))
                    _uiState.update { it.copy(message = "关联成功") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "关联失败: ${e.message}") }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun loadFabPosition() {
        viewModelScope.launch {
            val pos = dataStoreManager.getFabPosition("cabinet")
            _uiState.update { it.copy(fabPosition = pos) }
        }
    }

    fun saveFabPosition(x: Float, y: Float) {
        viewModelScope.launch {
            dataStoreManager.setFabPosition("cabinet", x, y)
            _uiState.update { it.copy(fabPosition = Pair(x, y)) }
        }
    }
}
