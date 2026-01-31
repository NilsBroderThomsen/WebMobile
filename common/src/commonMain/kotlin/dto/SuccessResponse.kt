package dto

import kotlinx.serialization.*

// DTO for success responses
@Serializable
data class SuccessResponse(
        val message: String,
        val data: String? = null
    )
