package com.example.myhealthconsultant.data.remote.api

import com.google.gson.annotations.SerializedName

// Request
data class GlmChatRequest(
    val model: String,
    val messages: List<GlmMessage>,
    val temperature: Double = 0.7,
    val stream: Boolean = false,
    @SerializedName("max_tokens") val maxTokens: Int = 2048
)

data class GlmMessage(
    val role: String,
    val content: String
)

// Response
data class GlmChatResponse(
    val id: String?,
    val model: String?,
    val choices: List<GlmChoice>?,
    val usage: GlmUsage?,
    val error: GlmError?
)

data class GlmChoice(
    val index: Int,
    val message: GlmAssistantMessage?,
    @SerializedName("finish_reason") val finishReason: String?
)

data class GlmAssistantMessage(
    val role: String,
    val content: String
)

data class GlmUsage(
    @SerializedName("prompt_tokens") val promptTokens: Int,
    @SerializedName("completion_tokens") val completionTokens: Int,
    @SerializedName("total_tokens") val totalTokens: Int
)

data class GlmError(
    val code: String,
    val message: String
)
