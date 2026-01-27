package dto

import kotlinx.serialization.*

@Serializable
data class CreateEntryRequest(
    val title: String,
    val content: String,
    val moodRating: Int? = null
)