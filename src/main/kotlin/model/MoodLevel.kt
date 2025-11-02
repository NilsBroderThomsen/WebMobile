package model

import extension.isValidMoodRating

enum class MoodLevel(val displayName: String, val range: IntRange, val emoji: String) {
    VERY_BAD("Sehr schlecht", 1..2, "😢"),
    BAD("Schlecht", 3..4, "😟"),
    NEUTRAL("Neutral", 5..6, "😐"),
    GOOD("Gut", 7..8, "😊"),
    VERY_GOOD("Sehr gut", 9..10, "😄");

    companion object {
        fun fromRating(rating: Int): MoodLevel? {
            require(rating.isValidMoodRating()) { "Mood rating must be between 1 and 10" }
            return entries.first { rating in it.range }
        }
    }
}
