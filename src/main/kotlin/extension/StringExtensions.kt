package extension

import model.Entry
import model.EntryId
import model.UserId
import model.EntryBuilder
import java.time.LocalDateTime

val String.isValidEmail: Boolean
    get() = this.contains("@") && this.contains(".")

val String.isValidUsername: Boolean
    get() = this.matches(Regex("^[A-Za-z0-9_]{3,20}$"))

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
    val moodRatingValue = parts[5].toIntOrNull()

    return EntryBuilder()
        .withId(idValue)
        .forUser(targetUserId)
        .createdAt(LocalDateTime.parse(createdAtValue))
        .withTitle(title)
        .withContent(content)
        .apply { moodRatingValue?.let { withMood(it) } }
        .build()
}