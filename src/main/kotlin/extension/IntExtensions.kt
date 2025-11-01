package extension

import model.MoodLevel

fun Int.isValidMoodRating(): Boolean =
    this in 1..10

fun Int.requireValidMoodRating(): Int = apply {
    require(isValidMoodRating()) { "Mood rating must be between 1 and 10" }
}

fun Int.toMoodLevel(): MoodLevel? =
    MoodLevel.fromRating(this)
