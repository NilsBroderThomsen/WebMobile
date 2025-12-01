package dto

import kotlinx.serialization.Serializable
import model.Entry

@Serializable
data class EntryDTO(
    val id: Long,
    val userId: Long,
    val title: String,
    val content: String,
    val moodRating: Int?,
    val createdAt: String,
    val updatedAt: String?
)

fun Entry.toDTO(): EntryDTO = EntryDTO(
    id = this.id.value,
    userId = this.userId.value,
    title = this.title,
    content = this.content,
    moodRating = this.moodRating,
    createdAt = this.createdAt.toString(),
    updatedAt = this.updatedAt?.toString()
)
