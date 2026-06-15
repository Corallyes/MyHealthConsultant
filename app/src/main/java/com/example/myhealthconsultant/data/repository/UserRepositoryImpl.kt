package com.example.myhealthconsultant.data.repository

import com.example.myhealthconsultant.data.local.dao.UserDao
import com.example.myhealthconsultant.data.local.entity.User
import com.example.myhealthconsultant.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override suspend fun getUserByPhone(phone: String): User? {
        return userDao.getUserByPhone(phone)
    }

    override suspend fun getUserByWechatId(openId: String): User? {
        return userDao.getUserByWechatId(openId)
    }

    override fun getUserById(userId: String): Flow<User?> {
        return userDao.getUserById(userId)
    }

    override suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    override suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    override suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }
}
