package com.example.myhealthconsultant.data.local.dao

import androidx.room.*
import com.example.myhealthconsultant.data.local.entity.ChatHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatHistoryDao {
    @Query("SELECT * FROM chat_history WHERE userId = :userId AND sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesBySession(userId: String, sessionId: String): Flow<List<ChatHistory>>

    @Query("SELECT DISTINCT sessionId FROM chat_history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getSessionIds(userId: String): Flow<List<String>>

    @Insert
    suspend fun insertMessage(message: ChatHistory)

    @Query("DELETE FROM chat_history WHERE userId = :userId AND sessionId = :sessionId")
    suspend fun deleteSession(userId: String, sessionId: String)

    @Query("DELETE FROM chat_history WHERE userId = :userId")
    suspend fun deleteAllMessages(userId: String)
}
