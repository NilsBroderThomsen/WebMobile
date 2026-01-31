package extension

import dto.EntryDto

fun List<EntryDto>.filterByMoodRange(minMood: Int?, maxMood: Int?): List<EntryDto> {
    if (minMood == null && maxMood == null) {
        return this
    }
    if (minMood != null && maxMood != null && minMood > maxMood) {
        return this
    }
    return filter { entry ->
        val rating = entry.moodRating ?: return@filter false
        val meetsMin = minMood?.let { rating >= it } ?: true
        val meetsMax = maxMood?.let { rating <= it } ?: true
        meetsMin && meetsMax
    }
}
