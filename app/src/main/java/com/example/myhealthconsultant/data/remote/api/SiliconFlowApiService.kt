package com.example.myhealthconsultant.data.remote.api

import retrofit2.http.Body
import retrofit2.http.POST

interface SiliconFlowApiService {
    @POST("v1/chat/completions")
    suspend fun chatCompletion(@Body request: GlmChatRequest): GlmChatResponse
}
