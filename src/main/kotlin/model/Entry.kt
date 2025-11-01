package model

import extension.isValidMoodRating
import extension.normalizeTag
import extension.requireValidMoodRating
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.roundToInt

@JvmInline
value class EntryId (val value: Long)

data class Entry (
    val id: EntryId,
    val userId: UserId,
    val title: String,
    val content: String,
    val moodRating: Int? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime? = null,
    val tags: Set<String> = emptySet()
) {
    init {
        moodRating?.requireValidMoodRating()
        val normalized = tags.map { it.normalizeTag() }.filter { it.isNotBlank() }.toSet()
        require(tags == normalized) { "Tags must be normalized and non-blank" }
    }

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
        return copy(moodRating = newRating.requireValidMoodRating(), updatedAt = LocalDateTime.now())
    }

    fun addTag(tag: String) = copy(tags = tags + tag.normalizeTag())

    fun removeTag(tag: String) = copy(tags = tags - tag.normalizeTag())

    fun similarity(other: Entry): Double {
        if (tags == other.tags) return 1.0
        if (tags.isEmpty() || other.tags.isEmpty()) return 0.0
        val intersection = tags.intersect(other.tags).size
        val union = tags.union(other.tags).size
        return intersection / union.toDouble()
    }
}

operator fun Entry.plus(other: Entry): Int {
    val firstMood = requireNotNull(moodRating) { "Both entries must have a mood rating" }
    val secondMood = requireNotNull(other.moodRating) { "Both entries must have a mood rating" }
    return ((firstMood + secondMood) / 2.0).roundToInt()
}