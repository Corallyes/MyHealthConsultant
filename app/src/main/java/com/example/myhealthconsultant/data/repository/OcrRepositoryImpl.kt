package com.example.myhealthconsultant.data.repository

import android.util.Base64
import com.example.myhealthconsultant.data.remote.api.OcrApiService
import com.example.myhealthconsultant.data.remote.api.OcrRequest
import com.example.myhealthconsultant.data.remote.api.OcrResponse
import com.example.myhealthconsultant.domain.repository.OcrRepository
import javax.inject.Inject

class OcrRepositoryImpl @Inject constructor(
    private val ocrApiService: OcrApiService
) : OcrRepository {

    override suspend fun recognizeImage(imageBytes: ByteArray): OcrResponse {
        val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        return ocrApiService.recognizeImage(OcrRequest(image = base64Image))
    }
}
