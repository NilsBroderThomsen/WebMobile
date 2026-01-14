package dto

import kotlinx.datetime.Clock
import kotlinx.serialization.*

// DTO für Error-Responses
@Serializable
data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: String = Clock.System.now().toString()
)
