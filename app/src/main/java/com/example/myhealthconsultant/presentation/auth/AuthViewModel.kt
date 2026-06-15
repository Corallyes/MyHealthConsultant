package com.example.myhealthconsultant.presentation.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhealthconsultant.data.local.entity.User
import com.example.myhealthconsultant.domain.repository.UserRepository
import com.example.myhealthconsultant.util.DataStoreManager
import com.example.myhealthconsultant.util.WeChatLoginHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val successMessage: String? = null,
    val verificationCode: String? = null,
    val codeSentMessage: String? = null,
    val savedPhone: String = "",
    val rememberMe: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkLoginStatus()
        loadSavedCredentials()
    }

    private fun loadSavedCredentials() {
        viewModelScope.launch {
            dataStoreManager.savedCredentials.collect { (remember, phone) ->
                if (remember && phone.isNotEmpty()) {
                    _uiState.update {
                        it.copy(savedPhone = phone, rememberMe = true)
                    }
                }
            }
        }
    }

    fun setRememberMe(remember: Boolean) {
        _uiState.update { it.copy(rememberMe = remember) }
        if (!remember) {
            viewModelScope.launch { dataStoreManager.clearCredentials() }
        }
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            dataStoreManager.isLoggedIn.collect { isLoggedIn ->
                _uiState.update { it.copy(isLoggedIn = isLoggedIn) }
            }
        }
    }

    fun login(phone: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val user = userRepository.getUserByPhone(phone)
                if (user != null && verifyPassword(password, user.passwordHash)) {
                    dataStoreManager.setLoggedInUserId(user.id)
                    if (_uiState.value.rememberMe) {
                        dataStoreManager.saveCredentials(phone)
                    } else {
                        dataStoreManager.clearCredentials()
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "手机号或密码错误"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "登录失败: ${e.message}"
                    )
                }
            }
        }
    }

    fun wechatLogin(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val result = WeChatLoginHelper.sendAuthRequest(context)

                if (result.isSuccess && result.code != null) {
                    val wechatId = result.code

                    // Check if user exists with this WeChat ID
                    val existingUser = userRepository.getUserByWechatId(wechatId)

                    if (existingUser != null) {
                        // Existing user - log in
                        dataStoreManager.setLoggedInUserId(existingUser.id)
                        _uiState.update {
                            it.copy(isLoading = false, isLoggedIn = true)
                        }
                    } else {
                        // New user - create account
                        val newUser = User(
                            phone = "wx_${System.currentTimeMillis()}",
                            passwordHash = hashPassword(UUID.randomUUID().toString()),
                            nickname = "微信用户",
                            wechatOpenId = wechatId
                        )
                        userRepository.insertUser(newUser)
                        dataStoreManager.setLoggedInUserId(newUser.id)
                        _uiState.update {
                            it.copy(isLoading = false, isLoggedIn = true)
                        }
                    }
                } else if (result.errCode == -100) {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.errMsg ?: "请先安装微信")
                    }
                } else if (result.errCode == -1) {
                    _uiState.update {
                        it.copy(isLoading = false, error = "已取消微信登录")
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.errMsg ?: "微信登录失败")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "微信登录失败: ${e.message}")
                }
            }
        }
    }

    fun sendVerificationCode(phone: String) {
        if (phone.length != 11) {
            _uiState.update { it.copy(error = "请输入正确的手机号") }
            return
        }

        val code = "123456"

        _uiState.update {
            it.copy(
                verificationCode = code,
                codeSentMessage = "验证码已发送，请查收短信",
                error = null
            )
        }

        println("Fake SMS sent to $phone, verification code: $code")
    }

    fun validateVerificationCode(inputCode: String): Boolean {
        return inputCode == _uiState.value.verificationCode
    }

    fun clearCodeSentMessage() {
        _uiState.update { it.copy(codeSentMessage = null) }
    }

    fun register(phone: String, password: String, nickname: String, inputCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                if (!validateVerificationCode(inputCode)) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "验证码错误，请重新输入"
                        )
                    }
                    return@launch
                }

                val existingUser = userRepository.getUserByPhone(phone)
                if (existingUser != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "该手机号已注册"
                        )
                    }
                    return@launch
                }

                val user = User(
                    phone = phone,
                    passwordHash = hashPassword(password),
                    nickname = nickname.ifEmpty { "用户${phone.takeLast(4)}" }
                )
                userRepository.insertUser(user)
                dataStoreManager.setLoggedInUserId(user.id)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        successMessage = "注册成功",
                        verificationCode = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "注册失败: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun resetPassword(phone: String, verificationCode: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                if (!validateVerificationCode(verificationCode)) {
                    _uiState.update { it.copy(isLoading = false, error = "验证码错误") }
                    return@launch
                }

                val user = userRepository.getUserByPhone(phone)
                if (user == null) {
                    _uiState.update { it.copy(isLoading = false, error = "该手机号未注册") }
                    return@launch
                }

                val updatedUser = user.copy(
                    passwordHash = hashPassword(newPassword),
                    updatedAt = System.currentTimeMillis()
                )
                userRepository.updateUser(updatedUser)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        verificationCode = null,
                        successMessage = "密码重置成功，请登录"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "重置失败: ${e.message}") }
            }
        }
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
