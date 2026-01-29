package extension

import model.Entry
import model.EntryId
import model.UserId
import kotlin.time.Instant

fun String.normalizeTag(): String =
    trim()
        .lowercase()
        .replace(Regex("\\s+"), " ")

fun String.sanitizeForCsv(): String =
    this.replace(",", "")
        .replace("\n", " ")
        .replace("\r", " ")
        .trim()

fun String.parseEntryFromCsv(targetUserId: UserId): Entry {
    val parts = split(",").map { it.trim() }
    require(parts.size == 6) { "Invalid CSV format: expected 6 columns but found ${parts.size}" }

    val idValue = EntryId(parts[0].toLong())
    val createdAtValue = parts[2]
    val title = parts[3]
    val content = parts[4]
    val moodField = parts[5]
    val moodRating = when {
        moodField.isEmpty() -> null
        else -> moodField.toIntOrNull()
            ?: throw IllegalArgumentException("Mood rating must be a number")
    }

    if (title.isBlank()) {
        throw IllegalArgumentException("Title must not be blank")
    }
    if (moodRating != null && !moodRating.isValidMoodRating()) {
        throw IllegalArgumentException("Mood rating must be between 1 and 10")
    }

    return Entry(
        id = idValue,
        userId = targetUserId,
        title = title,
        content = content,
        moodRating = moodRating,
        createdAt = Instant.parse(createdAtValue),
        updatedAt = null,
        tags = emptySet()
    )
}