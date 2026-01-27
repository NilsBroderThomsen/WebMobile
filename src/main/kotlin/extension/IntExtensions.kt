package extension

import model.MoodLevel

fun Int.isValidMoodRating(): Boolean =
    this in 1..10

fun Int.toMoodLevel(): MoodLevel? =
    MoodLevel.fromRating(this)

fun Int.toEmoji(): String = when (this) {
    in 1..3 -> "😢"
    in 4..5 -> "😐"
    in 6..7 -> "🙂"
    in 8..9 -> "😊"
    10 -> "🤩"
    else -> "❓"
}