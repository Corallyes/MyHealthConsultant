package com.example.myhealthconsultant.domain.repository

import com.example.myhealthconsultant.data.local.entity.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUserByPhone(phone: String): User?
    suspend fun getUserByWechatId(openId: String): User?
    fun getUserById(userId: String): Flow<User?>
    suspend fun insertUser(user: User)
    suspend fun updateUser(user: User)
    suspend fun deleteUser(user: User)
}
