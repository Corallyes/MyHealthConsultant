package com.example.myhealthconsultant.data.local.dao

import androidx.room.*
import com.example.myhealthconsultant.data.local.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): User?

    @Query("SELECT * FROM users WHERE wechatOpenId = :openId LIMIT 1")
    suspend fun getUserByWechatId(openId: String): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: String): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT * FROM users")
    suspend fun getAllUsersSync(): List<User>
}
