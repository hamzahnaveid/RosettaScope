package com.example.rosettascope.repositories

import com.example.rosettascope.api.RetrofitInstance
import com.example.rosettascope.api.TrainingBatchResponse
import com.example.rosettascope.api.TrainingRequest

class TrainingRepository {

    suspend fun getTraining(trainingWordBatch: List<String>, targetLanguage: String, confidenceMasteredBatch: List<Double>) : TrainingBatchResponse {
        return RetrofitInstance.api.getTraining(
            TrainingRequest(trainingWordBatch, targetLanguage, confidenceMasteredBatch)
        )
    }

}