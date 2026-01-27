package dto

import kotlinx.serialization.*
import serialization.LocalDateTimeSerializer
import java.time.LocalDateTime

// DTO für Entry in API-Responses
@Serializable
data class EntryDto(
    val id: Long,
    val userId: Long,
    val title: String,
    val content: String,
    val moodRating: Int?,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime?,
    val wordCount: Int,
    val moodLevel: String?
)