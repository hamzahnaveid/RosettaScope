package com.example.rosettascope.repositories

import com.example.rosettascope.api.GradingRequest
import com.example.rosettascope.api.GradingResponse
import com.example.rosettascope.api.RetrofitInstance

class GradingRepository {

    suspend fun gradeSpeech(refText: String, targetLanguage: String, recordingAudioBytes: String, confidenceMastered: Double) : GradingResponse {
        return RetrofitInstance.api.gradeSpeech(
            GradingRequest(refText, targetLanguage, recordingAudioBytes, confidenceMastered)
        )
    }
}