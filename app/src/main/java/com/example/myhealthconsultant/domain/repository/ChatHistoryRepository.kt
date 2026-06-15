package com.example.myhealthconsultant.domain.repository

import com.example.myhealthconsultant.data.local.entity.ChatHistory
import kotlinx.coroutines.flow.Flow

interface ChatHistoryRepository {
    fun getMessagesBySession(userId: String, sessionId: String): Flow<List<ChatHistory>>
    fun getSessionIds(userId: String): Flow<List<String>>
    suspend fun insertMessage(message: ChatHistory)
    suspend fun deleteSession(userId: String, sessionId: String)
    suspend fun deleteAllMessages(userId: String)
}
