package com.example.rosettascope.helpers

data class ConfidenceUpdateRequests(
    val email: String,
    val confidenceScores: Map<String, Double>
)