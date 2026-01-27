package dto

import kotlinx.serialization.*

// DTO für Entry-Erstellung (Request Body)
@Serializable
data class CreateEntryRequest(
    val title: String,
    val content: String,
    val moodRating: Int? = null
)