package dto

import kotlinx.serialization.*

@Serializable
data class EntryDto(
    val id: Long,
    val userId: Long,
    val title: String,
    val content: String,
    val moodRating: Int?,
    val createdAt: String,
    val updatedAt: String?
)
