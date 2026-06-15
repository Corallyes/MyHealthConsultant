package com.example.myhealthconsultant.domain.repository

data class ChatMessage(val role: String, val content: String)

interface AiRepository {
    suspend fun chat(systemPrompt: String, userMessage: String, modelId: String): String
    suspend fun chatWithHistory(systemPrompt: String, history: List<ChatMessage>, modelId: String): String
}
