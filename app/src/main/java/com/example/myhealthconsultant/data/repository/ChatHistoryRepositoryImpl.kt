package com.example.myhealthconsultant.data.repository

import com.example.myhealthconsultant.data.local.dao.ChatHistoryDao
import com.example.myhealthconsultant.data.local.entity.ChatHistory
import com.example.myhealthconsultant.domain.repository.ChatHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatHistoryRepositoryImpl @Inject constructor(
    private val dao: ChatHistoryDao
) : ChatHistoryRepository {

    override fun getMessagesBySession(userId: String, sessionId: String): Flow<List<ChatHistory>> = 
        dao.getMessagesBySession(userId, sessionId)

    override fun getSessionIds(userId: String): Flow<List<String>> = dao.getSessionIds(userId)

    override suspend fun insertMessage(message: ChatHistory) = dao.insertMessage(message)

    override suspend fun deleteSession(userId: String, sessionId: String) = dao.deleteSession(userId, sessionId)

    override suspend fun deleteAllMessages(userId: String) = dao.deleteAllMessages(userId)
}
