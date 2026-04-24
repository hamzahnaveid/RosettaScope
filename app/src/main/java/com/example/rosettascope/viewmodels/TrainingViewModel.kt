package com.example.rosettascope.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rosettascope.api.TrainingBatchResponse
import com.example.rosettascope.repositories.TrainingRepository
import kotlinx.coroutines.launch

class TrainingViewModel : ViewModel() {
    private val trainingRepository = TrainingRepository()
    val trainingResult = MutableLiveData<TrainingBatchResponse>()
    val errorMessage = MutableLiveData<String>()

    fun getTraining(wordBatch: List<String>, targetLanguage: String, confidenceMasteredBatch: List<Double>) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getTraining(wordBatch, targetLanguage, confidenceMasteredBatch)
                trainingResult.postValue(response)
            } catch (e: Exception) {
                errorMessage.postValue(e.message)
            }
        }
    }
}