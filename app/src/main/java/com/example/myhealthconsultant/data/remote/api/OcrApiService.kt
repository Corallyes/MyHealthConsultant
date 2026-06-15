package com.example.myhealthconsultant.data.remote.api

import retrofit2.http.Body
import retrofit2.http.POST

interface OcrApiService {
    @POST("/api/ocr")
    suspend fun recognizeImage(@Body request: OcrRequest): OcrResponse
}
