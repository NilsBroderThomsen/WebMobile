package dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateEntryRequest(
    val title: String,
    val content: String,
    val moodRating: Int? = null
)
