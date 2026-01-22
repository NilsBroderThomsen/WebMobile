package dto

import kotlinx.serialization.*

// DTO für Success-Responses
@Serializable
data class SuccessResponse(
        val message: String,
        val data: String? = null
    )