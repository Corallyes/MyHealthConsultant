package com.example.myhealthconsultant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhealthconsultant.data.local.entity.User
import com.example.myhealthconsultant.domain.repository.UserRepository
import com.example.myhealthconsultant.util.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val userId = dataStoreManager.getLoggedInUserId()
            if (userId != null) {
                userRepository.getUserById(userId).collect { user ->
                    _currentUser.value = user
                }
            }
        }
    }
}
