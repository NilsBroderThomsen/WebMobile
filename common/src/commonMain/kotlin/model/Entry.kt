package model

import kotlin.math.roundToInt
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

@JvmInline
value class EntryId (val value: Long)

data class Entry (
    val id: EntryId,
    val userId: UserId,
    val title: String,
    val content: String,
    val moodRating: Int? = null,
    val createdAt: Instant,
    val updatedAt: Instant? = null,
    val tags: Set<String> = emptySet()
) {

    val wordCount: Int
        get() = content.split(Regex("\\s")).count { it.isNotBlank() }

    val hasGoodMood: Boolean
        get() = moodRating?.let { it >= 7 } ?: false

    val hasPoorMood: Boolean
        get() = moodRating?.let { it <= 3 } ?: false

    val isEdited: Boolean
        get() = updatedAt != null

    val createdDate: LocalDate
        get() {
            return createdAt.toLocalDateTime(TimeZone.UTC).date
        }

    fun updateContent(newContent: String):
        Entry = copy(content = newContent, updatedAt = Clock.System.now())

    fun updateMood(newRating: Int): Entry {
        return copy(moodRating = newRating, updatedAt = Clock.System.now())
    }

    fun addTag(tag: String): Entry {
        require(tag.isNotBlank()) { "Tag must not be blank" }
        return copy(tags = tags + tag)
    }

    fun removeTag(tag: String): Entry {
        require(tag.isNotBlank()) { "Tag must not be blank" }
        return copy(tags = tags - tag)
    }

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