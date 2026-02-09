package com.example.rosettascope.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("email") val email: String,
    @SerializedName("password") var password: String,
    @SerializedName("proficiency") var proficiency: String,
    @SerializedName("targetLanguage") var targetLanguage: String,
    @SerializedName("wordsEncountered") var wordsEncountered: Int,
    @SerializedName("wordsMastered") var wordsMastered: Int,
    @SerializedName("scores") val scores: ArrayList<Score>,
    @SerializedName("confidenceScores") val confidenceScores: MutableMap<String, Double>
)