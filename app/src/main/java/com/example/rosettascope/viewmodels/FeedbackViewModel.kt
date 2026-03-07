package com.example.rosettascope.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rosettascope.api.FeedbackResponse
import com.example.rosettascope.repositories.FeedbackRepository
import kotlinx.coroutines.launch

class FeedbackViewModel : ViewModel() {

    private val feedbackRepository = FeedbackRepository()

    val feedbackResult = MutableLiveData<FeedbackResponse>()
    val errorMessage = MutableLiveData<String>()

    fun getFeedback(feedbackJsonArray: String) {
        viewModelScope.launch {
            try {
                val response = feedbackRepository.getFeedback(feedbackJsonArray)
                feedbackResult.postValue(response)
            } catch (e: Exception) {
                errorMessage.postValue(e.message)
            }
        }
    }
}