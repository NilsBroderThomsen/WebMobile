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
    id = id.value,
    userId = userId.value,
    title = title,
    content = content,
    moodRating = moodRating,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt?.toString()
)
