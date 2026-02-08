package com.example.rosettascope.api

import retrofit2.http.Body
import retrofit2.http.POST

data class TranslationRequest(
    val word: String,
    val targetLanguage: String
)

data class TranslationResponse(
    val translation: String,
    val translated_word: String,
    val original_text: String,
    val pronunciation_audio_base64: String? = null
)

data class GradingRequest(
    val refText: String,
    val targetLanguage: String,
    val recordingAudioBytes: String
)

data class GradingResponse(
    val result: String,
    val feedback: String
)

interface TranslationApiService {
    @POST("/translate")
    suspend fun translateWord(@Body request: TranslationRequest): TranslationResponse

    @POST("/grade")
    suspend fun gradeSpeech(@Body request: GradingRequest): GradingResponse
}