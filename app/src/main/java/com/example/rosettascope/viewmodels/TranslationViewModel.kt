package com.example.rosettascope.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rosettascope.api.TranslationResponse
import com.example.rosettascope.repositories.TranslationRepository
import kotlinx.coroutines.launch

class TranslationViewModel : ViewModel() {
    private val translationRepository = TranslationRepository()

    val translationResult = MutableLiveData<TranslationResponse>()
    val errorMessage = MutableLiveData<String>()

    fun translateWord(word: String, targetLanguage: String) {
        viewModelScope.launch {
            try {
                val response = translationRepository.translateWord(word, targetLanguage)
                translationResult.postValue(response)
            } catch (e: Exception) {
                errorMessage.postValue(e.message)
            }
        }
    }
}