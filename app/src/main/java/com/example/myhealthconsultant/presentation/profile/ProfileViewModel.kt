package com.example.myhealthconsultant.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhealthconsultant.data.local.entity.User
import com.example.myhealthconsultant.domain.repository.UserRepository
import com.example.myhealthconsultant.util.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val consecutiveDays: Int = 7,
    val totalCheckIns: Int = 21,
    val isDarkMode: Boolean = false,
    val isNotificationEnabled: Boolean = true,
    val reminderAdvanceMinutes: Int = 10,
    val aiProvider: String = "通义千问",
    val isLoading: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserInfo()
        loadSettings()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            val userId = dataStoreManager.getLoggedInUserId()
            if (userId != null) {
                userRepository.getUserById(userId).collect { user ->
                    _uiState.update { it.copy(user = user) }
                }
            }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            dataStoreManager.isDarkMode.collect { isDarkMode ->
                _uiState.update { it.copy(isDarkMode = isDarkMode) }
            }
        }
        viewModelScope.launch {
            dataStoreManager.aiProvider.collect { provider ->
                _uiState.update { it.copy(aiProvider = provider) }
            }
        }
        viewModelScope.launch {
            dataStoreManager.isNotificationEnabled.collect { enabled ->
                _uiState.update { it.copy(isNotificationEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            dataStoreManager.reminderAdvanceMinutes.collect { minutes ->
                _uiState.update { it.copy(reminderAdvanceMinutes = minutes) }
            }
        }
    }

    fun toggleNotification() {
        viewModelScope.launch {
            val newValue = !_uiState.value.isNotificationEnabled
            dataStoreManager.setNotificationEnabled(newValue)
            _uiState.update { it.copy(isNotificationEnabled = newValue) }
        }
    }

    fun setReminderAdvanceMinutes(minutes: Int) {
        viewModelScope.launch {
            dataStoreManager.setReminderAdvanceMinutes(minutes)
            _uiState.update { it.copy(reminderAdvanceMinutes = minutes) }
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            val newValue = !_uiState.value.isDarkMode
            dataStoreManager.setDarkMode(newValue)
            _uiState.update { it.copy(isDarkMode = newValue) }
        }
    }

    fun setAiProvider(provider: String) {
        viewModelScope.launch {
            dataStoreManager.setAiProvider(provider)
            _uiState.update { it.copy(aiProvider = provider) }
        }
    }

    /**
     * 更新头像
     */
    fun updateAvatar(avatarUrl: String) {
        viewModelScope.launch {
            try {
                val currentUser = _uiState.value.user ?: return@launch
                val updatedUser = currentUser.copy(
                    avatarUrl = avatarUrl,
                    updatedAt = System.currentTimeMillis()
                )
                userRepository.updateUser(updatedUser)
                _uiState.update { it.copy(message = "头像更新成功") }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "头像更新失败: ${e.message}") }
            }
        }
    }

    /**
     * 更新昵称
     */
    fun updateNickname(newNickname: String) {
        viewModelScope.launch {
            try {
                val currentUser = _uiState.value.user ?: return@launch
                val updatedUser = currentUser.copy(
                    nickname = newNickname,
                    updatedAt = System.currentTimeMillis()
                )
                userRepository.updateUser(updatedUser)
                _uiState.update { it.copy(message = "昵称修改成功") }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "修改失败: ${e.message}") }
            }
        }
    }

    /**
     * 更新手机号
     */
    fun updatePhone(newPhone: String) {
        viewModelScope.launch {
            try {
                val currentUser = _uiState.value.user ?: return@launch
                // 检查手机号是否已被使用
                val existingUser = userRepository.getUserByPhone(newPhone)
                if (existingUser != null && existingUser.id != currentUser.id) {
                    _uiState.update { it.copy(message = "该手机号已被其他账号使用") }
                    return@launch
                }
                val updatedUser = currentUser.copy(
                    phone = newPhone,
                    updatedAt = System.currentTimeMillis()
                )
                userRepository.updateUser(updatedUser)
                _uiState.update { it.copy(message = "手机号修改成功") }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "修改失败: ${e.message}") }
            }
        }
    }

    /**
     * 更新密码
     */
    fun updatePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                val currentUser = _uiState.value.user ?: return@launch
                // 验证原密码
                if (!verifyPassword(oldPassword, currentUser.passwordHash)) {
                    _uiState.update { it.copy(message = "原密码错误") }
                    return@launch
                }
                val updatedUser = currentUser.copy(
                    passwordHash = hashPassword(newPassword),
                    updatedAt = System.currentTimeMillis()
                )
                userRepository.updateUser(updatedUser)
                _uiState.update { it.copy(message = "密码修改成功") }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "修改失败: ${e.message}") }
            }
        }
    }

    /**
     * 清除消息
     */
    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun logout() {
        viewModelScope.launch {
            dataStoreManager.clearLoginState()
        }
    }

    private fun hashPassword(password: String): String {
        val salt = UUID.randomUUID().toString().take(8)
        val hash = MessageDigest.getInstance("SHA-256")
            .digest("$salt$password".toByteArray())
            .joinToString("") { "%02x".format(it) }
        return "$salt:$hash"
    }

    private fun verifyPassword(password: String, storedHash: String): Boolean {
        val parts = storedHash.split(":")
        if (parts.size != 2) return false
        val (salt, hash) = parts
        val computedHash = MessageDigest.getInstance("SHA-256")
            .digest("$salt$password".toByteArray())
            .joinToString("") { "%02x".format(it) }
        return computedHash == hash
    }
}
