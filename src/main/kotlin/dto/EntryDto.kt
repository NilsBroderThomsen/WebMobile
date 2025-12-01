package dto

import kotlinx.serialization.Serializable

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
