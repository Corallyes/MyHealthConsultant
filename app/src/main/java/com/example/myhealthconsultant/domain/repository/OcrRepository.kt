package com.example.myhealthconsultant.domain.repository

import com.example.myhealthconsultant.data.remote.api.OcrResponse

interface OcrRepository {
    suspend fun recognizeImage(imageBytes: ByteArray): OcrResponse
}
