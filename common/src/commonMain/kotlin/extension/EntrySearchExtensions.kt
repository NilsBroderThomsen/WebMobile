package extension

import dto.EntryDto

fun List<EntryDto>.filterBySearchQuery(query: String): List<EntryDto> {
    val trimmedQuery = query.trim()
    if (trimmedQuery.isEmpty()) {
        return this
    }
    val normalizedQuery = trimmedQuery.lowercase()
    return filter { entry ->
        entry.title.contains(normalizedQuery, ignoreCase = true) ||
            entry.content.contains(normalizedQuery, ignoreCase = true) ||
            (entry.moodRating?.toString()?.contains(normalizedQuery) == true)
    }
}
