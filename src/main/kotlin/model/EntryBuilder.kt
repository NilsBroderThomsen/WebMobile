package model

import extension.normalizeTag
import java.time.LocalDateTime

class EntryBuilder {
    private var id: EntryId = EntryId(0)
    private var userId: UserId? = null
    private var title: String = ""
    private var content: String = ""
    private var moodRating: Int? = null
    private var createdAt: LocalDateTime = LocalDateTime.now()
    private var updatedAt: LocalDateTime? = null
    private val tags: MutableSet<String> = linkedSetOf()

    fun withId(id: EntryId) = apply { this.id = id }

    fun forUser(userId: UserId) = apply { this.userId = userId }

    fun withTitle(title: String) = apply { this.title = title }

    fun withContent(content: String) = apply { this.content = content }

    fun withMood(rating: Int) = apply {
        require(rating in 1..10) { "Mood rating must be between 1 and 10" }
        this.moodRating = rating
    }

    fun createdAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }

    fun updatedAt(updatedAt: LocalDateTime?) = apply { this.updatedAt = updatedAt }

    fun addTag(tag: String) = apply {
        val normalized = tag.normalizeTag()
        if (normalized.isNotBlank()) {
            tags += normalized
        }
    }

    fun addTags(vararg tags: String) = apply { tags.forEach { addTag(it) } }

    fun clearTags() = apply { tags.clear() }

    fun build(): Entry {
        val resolvedUserId = requireNotNull(userId) { "User must be specified" }
        require(title.isNotBlank()) { "Title must not be blank" }
        return Entry(
            id = id,
            userId = resolvedUserId,
            title = title,
            content = content,
            moodRating = moodRating,
            createdAt = createdAt,
            updatedAt = updatedAt,
            tags = tags.toSet()
        )
    }
}
