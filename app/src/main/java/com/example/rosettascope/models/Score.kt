package com.example.rosettascope.models

import com.google.gson.annotations.SerializedName

data class Score(
    @SerializedName("id") val id: Int?,
    @SerializedName("word") val word: String,
    @SerializedName("language") val language: String,
    @SerializedName("score") val score: Int,
    @SerializedName("engWord") val engWord: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("feedback") val feedback: String,
    @SerializedName("trained") val trained: Boolean?
)
