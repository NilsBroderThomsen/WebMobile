package dto

import java.time.Instant
import kotlinx.serialization.*

// DTO für Error-Responses
@Serializable
data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: String =  Instant.now().toString()
)
