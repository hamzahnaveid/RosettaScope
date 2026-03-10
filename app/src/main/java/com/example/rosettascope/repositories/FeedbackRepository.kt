package com.example.rosettascope.repositories

import com.example.rosettascope.api.FeedbackRequest
import com.example.rosettascope.api.FeedbackResponse
import com.example.rosettascope.api.RetrofitInstance

class FeedbackRepository {

    suspend fun getFeedback(feedbackJsonArray: String) : FeedbackResponse {
        return RetrofitInstance.api.getFeedback(
            FeedbackRequest(feedbackJsonArray)
        )
    }
}