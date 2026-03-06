package com.example.rosettascope.helpers

data class ScoreRequest(
    val email: String,
    val word: String,
    val language: String,
    val score: Int,
    val engWord: String,
    val timestamp: Long,
    val confidenceScore: Double
)