package extension

import model.Entry
import model.EntryId
import model.UserId
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
    this.replace(",", "").replace("\n", " ").replace("\r", " ").trim()

fun String.parseEntryFromCsv(targetUserId: UserId): Entry {
    val parts = split(",")
    return Entry(EntryId(parts[0].toLong()),
        targetUserId,
        parts[3],
        parts[4],
        parts[5].toIntOrNull(),
        LocalDateTime.parse(parts[2]),
        null,
        emptySet())
}