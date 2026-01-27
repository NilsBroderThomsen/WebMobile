package dto

import kotlinx.serialization.*
import java.time.LocalDateTime

// DTO für Error-Responses
@Serializable
data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: String = LocalDateTime.now().toString()
)