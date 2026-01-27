package extension

import model.MoodLevel

fun Int.isValidMoodRating(): Boolean =
    this in 1..10

fun Int.toMoodLevel(): MoodLevel? =
    MoodLevel.fromRating(this)