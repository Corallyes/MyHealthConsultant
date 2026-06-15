package com.example.myhealthconsultant.data.repository

import android.util.Log
import com.example.myhealthconsultant.data.remote.api.GlmApiService
import com.example.myhealthconsultant.data.remote.api.GlmChatRequest
import com.example.myhealthconsultant.data.remote.api.GlmMessage
import com.example.myhealthconsultant.data.remote.api.SiliconFlowApiService
import com.example.myhealthconsultant.domain.repository.AiRepository
import com.example.myhealthconsultant.domain.repository.ChatMessage
import retrofit2.HttpException
import javax.inject.Inject

private const val TAG = "AiRepositoryImpl"

class AiRepositoryImpl @Inject constructor(
    private val glmApiService: GlmApiService,
    private val siliconFlowApiService: SiliconFlowApiService
) : AiRepository {

    private fun resolveModelId(modelId: String): String = when (modelId) {
        "qwen3-8b" -> "Qwen/Qwen3-8B"
        else -> "glm-4-flash"
    }

    private fun isSiliconFlow(modelId: String) = modelId == "qwen3-8b"

    private suspend fun executeRequest(request: GlmChatRequest, modelId: String): String {
        Log.d(TAG, "executeRequest: model=${request.model}, isSiliconFlow=${isSiliconFlow(modelId)}")
        Log.d(TAG, "Request body: model=${request.model}, messages=${request.messages.size}, temp=${request.temperature}, stream=${request.stream}, maxTokens=${request.maxTokens}")
        request.messages.forEachIndexed { index, msg ->
            Log.d(TAG, "  msg[$index]: role=${msg.role}, content=${msg.content.take(50)}...")
        }

        return try {
            val response = if (isSiliconFlow(modelId)) {
                Log.d(TAG, "Calling SiliconFlow API")
                siliconFlowApiService.chatCompletion(request)
            } else {
                Log.d(TAG, "Calling GLM API")
                glmApiService.chatCompletion(request)
            }

            Log.d(TAG, "Response received: id=${response.id}, choices=${response.choices?.size}")

            response.error?.let {
                Log.e(TAG, "API error: ${it.code} - ${it.message}")
                throw Exception("API错误: ${it.message}")
            }

            val content = response.choices
                ?.firstOrNull()
                ?.message
                ?.content

            if (content.isNullOrEmpty()) {
                Log.e(TAG, "Empty response content")
                throw Exception("AI返回了空响应，请重试")
            }

            Log.d(TAG, "Response content length: ${content.length}")
            content
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e(TAG, "HTTP error ${e.code()}: $errorBody")
            throw Exception("API错误 (${e.code()}): $errorBody")
        } catch (e: Exception) {
            Log.e(TAG, "executeRequest failed", e)
            throw e
        }
    }

    override suspend fun chat(systemPrompt: String, userMessage: String, modelId: String): String {
        val request = GlmChatRequest(
            model = resolveModelId(modelId),
            messages = listOf(
                GlmMessage(role = "system", content = systemPrompt),
                GlmMessage(role = "user", content = userMessage)
            )
        )
        return executeRequest(request, modelId)
    }

    override suspend fun chatWithHistory(systemPrompt: String, history: List<ChatMessage>, modelId: String): String {
        val messages = mutableListOf(GlmMessage(role = "system", content = systemPrompt))
        history.forEach { msg ->
            messages.add(GlmMessage(role = msg.role, content = msg.content))
        }

        Log.d(TAG, "chatWithHistory: ${messages.size} messages, modelId=$modelId")

        val request = GlmChatRequest(
            model = resolveModelId(modelId),
            messages = messages
        )
        return executeRequest(request, modelId)
    }
}
