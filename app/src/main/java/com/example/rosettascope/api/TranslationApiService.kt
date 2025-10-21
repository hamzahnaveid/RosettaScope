package com.example.rosettascope.api

import retrofit2.http.Body
import retrofit2.http.POST

data class TranslationRequest(
    val word: String,
    val targetLanguage: String
)

data class TranslationResponse(
    val translated_word: String,
    val pronunciation_audio_base64: String? = null
)

interface TranslationApiService {
    @POST("/translate")
    suspend fun translateWord(@Body request: TranslationRequest): TranslationResponse
}