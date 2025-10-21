package com.example.rosettascope.repositories

import com.example.rosettascope.api.RetrofitInstance
import com.example.rosettascope.api.TranslationRequest
import com.example.rosettascope.api.TranslationResponse

class TranslationRepository {
    suspend fun translateWord(word: String, targetLanguage: String) : TranslationResponse {
        return RetrofitInstance.api.translateWord(
            TranslationRequest(word, targetLanguage)
        )
    }
}