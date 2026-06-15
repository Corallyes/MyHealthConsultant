package com.example.myhealthconsultant.presentation.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhealthconsultant.data.local.entity.ScanHistory
import com.example.myhealthconsultant.domain.repository.ScanHistoryRepository
import com.example.myhealthconsultant.util.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ScanHistoryUiState(
    val scans: List<ScanHistoryItem> = emptyList(),
    val isLoading: Boolean = false
)

data class ScanHistoryItem(
    val scan: ScanHistory,
    val dateText: String,
    val timeText: String
)

@HiltViewModel
class ScanHistoryViewModel @Inject constructor(
    private val scanHistoryRepository: ScanHistoryRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanHistoryUiState())
    val uiState: StateFlow<ScanHistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val userId = dataStoreManager.getLoggedInUserId() ?: return@launch
            _uiState.update { it.copy(isLoading = true) }
            scanHistoryRepository.getAllScans(userId).collect { scans ->
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val items = scans.map { scan ->
                    val cal = Calendar.getInstance().apply { timeInMillis = scan.scannedAt }
                    ScanHistoryItem(
                        scan = scan,
                        dateText = dateFormat.format(cal.time),
                        timeText = timeFormat.format(cal.time)
                    )
                }
                _uiState.update { it.copy(scans = items, isLoading = false) }
            }
        }
    }

    fun deleteScan(scan: ScanHistory) {
        viewModelScope.launch {
            scanHistoryRepository.deleteScan(scan)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            val userId = dataStoreManager.getLoggedInUserId() ?: return@launch
            scanHistoryRepository.deleteAllScans(userId)
        }
    }
}
