package com.example.myhealthconsultant.data.remote.api

data class OcrRequest(
    val image: String  // base64 encoded image
)

data class OcrResponse(
    val texts: List<String>,
    val scores: List<Float>,
    val success: Boolean,
    val error: String? = null
)
