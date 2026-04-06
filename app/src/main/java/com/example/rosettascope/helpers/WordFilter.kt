package com.example.rosettascope.helpers

enum class WordFilter {
    ALL,
    UNDISCOVERED,
    DISCOVERED,
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    MASTERED
}

fun getFilter(word: String, discoveredWords: Map<String, Double>): WordFilter {
    if (!discoveredWords.containsKey(word)) return WordFilter.UNDISCOVERED

    val score = (discoveredWords[word]!! * 100).toInt()

    return when (score) {
        in 0..24 -> WordFilter.BEGINNER
        in 25..76 -> WordFilter.INTERMEDIATE
        in 77..94 -> WordFilter.ADVANCED
        in 95..100 -> WordFilter.MASTERED
        else -> WordFilter.DISCOVERED
    }
}