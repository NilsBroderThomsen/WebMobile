package model

import extension.normalizeTag
import java.time.LocalDate
import java.time.LocalDateTime

@JvmInline
value class EntryId (val value: Long)

data class Entry (
    val id: EntryId,
    val userId: UserId,
    val title: String,
    val content:  String,
    val moodRating: Int? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
    val tags: Set<String>
) {
    val wordCount: Int
        get() = content.split(Regex("\\s")).count() { it.isNotBlank() }

    val hasGoodMood: Boolean
        get() = moodRating?.let { it >= 7 } ?: false

    val hasPoorMood: Boolean
        get() = moodRating?.let { it <= 3 } ?: false

    val isEdited: Boolean
        get() = updatedAt != null

    val createdDate: LocalDate
        get() = createdAt.toLocalDate()

    fun updateContent(newContent: String):
        Entry = copy(content = newContent, updatedAt = LocalDateTime.now())

    fun updateMood(newRating: Int): Entry {
        require(newRating in 1..10) { "Mood rating must be between 1 and 10" }
        return copy(moodRating = newRating, updatedAt = LocalDateTime.now())
    }

    fun addTag(tag: String) = copy(tags = tags + tag.normalizeTag())

    fun removeTag(tag: String) = copy(tags = tags - tag.normalizeTag())
}