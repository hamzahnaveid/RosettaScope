package com.example.rosettascope.models

data class TrainingItem(
    val word: String,
    val translation: String,
    val speakingText: String,
    val speakingAudio: String?,
    val listeningText: String,
    val listeningFluffWords: List<String>,
    val listeningAudio: String?,
    val readingText: String,
    val readingAnswer: String
)
