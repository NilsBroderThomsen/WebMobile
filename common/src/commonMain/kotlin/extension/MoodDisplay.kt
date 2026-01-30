package extension

import dto.EntryDto

fun displayMood(
    moodRating: Int?,
    unknownText: String,
    formatRating: (Int) -> String = { it.toString() }
): String {
    val rating = moodRating ?: return unknownText
    return "${formatRating(rating)} ${rating.toEmoji()}".trim()
}

fun EntryDto.displayMood(
    unknownText: String,
    formatRating: (Int) -> String = { it.toString() }
): String {
    return displayMood(moodRating, unknownText, formatRating)
}
