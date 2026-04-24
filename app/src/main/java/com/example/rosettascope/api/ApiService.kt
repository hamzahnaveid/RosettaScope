package com.example.rosettascope.api

import retrofit2.http.Body
import retrofit2.http.POST

data class TranslationRequest(
    val word: String,
    val targetLanguage: String,
    val confidenceMastered: Double
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
    val recordingAudioBytes: String,
    val confidenceMastered: Double
)

data class GradingResponse(
    val result: String,
    val feedback: String,
    val new_confidence_mastered: Double,
    val is_correct: String
)

data class FeedbackRequest(
    val feedbackJsonArray: String
)

data class FeedbackResponse(
    val feedback: String
)

data class TrainingRequest(
    val trainingWord: List<String>,
    val targetLanguage: String,
    val confidenceMastered: List<Double>,
)

data class TrainingResponse(
    val word: String,
    val translation: String,
    val speaking_text: String,
    val speaking_pronunciation_audio_base64: String? = null,
    val listening_text: String,
    val listening_fluff_words: List<String>,
    val listening_pronunciation_audio_base64: String? = null,
    val reading_text: String,
    val reading_answer: String,
)

data class TrainingBatchResponse(
    val results: List<TrainingResponse>
)


interface ApiService {
    @POST("/translate")
    suspend fun translateWord(@Body request: TranslationRequest): TranslationResponse

    @POST("/grade")
    suspend fun gradeSpeech(@Body request: GradingRequest): GradingResponse

    @POST("/feedback")
    suspend fun getFeedback(@Body request: FeedbackRequest): FeedbackResponse

    @POST("/training")
    suspend fun getTraining(@Body request: TrainingRequest): TrainingBatchResponse
}