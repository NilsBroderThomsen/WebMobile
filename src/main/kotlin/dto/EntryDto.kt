package dto

import kotlin.time.Instant
import kotlinx.serialization.*

@Serializable
data class EntryDto(
    val id: Long,
    val userId: Long,
    val title: String,
    val content: String,
    val moodRating: Int?,
    val createdAt: Instant,
    val updatedAt: Instant?,
    val tags: Set<String>
)