package dto

import kotlinx.serialization.*

// DTO für Error-Responses
@Serializable
data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: String =  kotlin.time.Clock.System.now().toString()
)