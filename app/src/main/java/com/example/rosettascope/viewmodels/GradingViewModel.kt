package com.example.rosettascope.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rosettascope.api.GradingResponse
import com.example.rosettascope.repositories.GradingRepository
import kotlinx.coroutines.launch

class GradingViewModel : ViewModel() {
    private val gradingRepository = GradingRepository()

    val gradingResult = MutableLiveData<GradingResponse>()
    val errorMessage = MutableLiveData<String>()

    fun gradeSpeech(refText: String, targetLanguage: String, recordingAudioBytes: String) {
        viewModelScope.launch {
            try {
                val response = gradingRepository.gradeSpeech(refText, targetLanguage, recordingAudioBytes)
                gradingResult.postValue(response)
            } catch (e: Exception) {
                errorMessage.postValue(e.message)
            }
        }
    }


}