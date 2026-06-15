package com.example.myhealthconsultant.data.remote.api

import retrofit2.http.Body
import retrofit2.http.POST

interface GlmApiService {
    @POST("paas/v4/chat/completions")
    suspend fun chatCompletion(@Body request: GlmChatRequest): GlmChatResponse
}
